package com.lsd.reconciliation;

import com.lsd.stateMachine.StateMachineApplication;
import com.lsd.stateMachine.engine.OrderFsmEngine;
import com.lsd.stateMachine.enums.ErrorCodeEnum;
import com.lsd.stateMachine.exception.FsmException;
import com.lsd.stateMachine.plugin.DefaultPluginRegistry;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.checker.RiskChecker;
import com.lsd.reconciliation.enums.ReconciliationStateEnum;
import com.lsd.reconciliation.event.ReconciliationEvent;
import com.lsd.reconciliation.plugin.AuditLogPluginHandler;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import com.lsd.reconciliation.service.ReconciliationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 银企对账状态流转集成测试。
 *
 * <p>覆盖：
 * <ol>
 *   <li>平账链路（金额相等 → 动态流转到 CONFIRMED）；</li>
 *   <li>差异链路（金额不等 → DIFFERENT → 人工确认 → CONFIRMED）；</li>
 *   <li>动态下一状态（getNextState）；</li>
 *   <li>乐观锁校验（期望状态不匹配抛异常）；</li>
 *   <li>未找到处理器异常；</li>
 *   <li>插件机制修复实证（插件被注册且执行）；</li>
 *   <li>释放逻辑反转修复实证（needRelease 的 checker 真被 release）。</li>
 * </ol>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StateMachineApplication.class)
public class ReconciliationFlowTest {

    @Autowired
    private ReconciliationService reconciliationService;

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @Autowired
    private DefaultPluginRegistry pluginRegistry;

    @Before
    public void setUp() {
        RiskChecker.resetReleaseCount();
        AuditLogPluginHandler.resetExecutedCount();
    }

    /** 1. 平账链路：金额相等，比对完成后直接 CONFIRMED */
    @Test
    public void testBalancedFlow() {
        ReconciliationBill bill = reconciliationService.runBalancedFlow(
                "BILL-BAL-001", "ICBC", "DAILY", "ACC-123", new BigDecimal("10000.00"));

        assertEquals("平账链路最终状态应为 CLOSED", ReconciliationStateEnum.CLOSED.name(), bill.getOrderState());
        assertEquals("平账差异笔数应为 0", 0, bill.getDifferenceCount());
        assertTrue("插件应至少执行一次", AuditLogPluginHandler.getExecutedCount() >= 1);
    }

    /** 2. 差异链路：金额不等 → DIFFERENT → 人工确认 → CLOSED */
    @Test
    public void testDifferenceFlow() {
        ReconciliationBill bill = reconciliationService.runDifferenceFlow(
                "BILL-DIFF-001", "CCB", "MONTHLY", "ACC-456",
                new BigDecimal("10000.00"), new BigDecimal("9950.00"));

        assertEquals("差异链路最终状态应为 CLOSED", ReconciliationStateEnum.CLOSED.name(), bill.getOrderState());
        assertEquals("确认后差异笔数应清零", 0, bill.getDifferenceCount());
    }

    /** 3. 逐步验证动态下一状态：COMPARING --FINISH_COMPARE--> CONFIRMED / DIFFERENT */
    @Test
    public void testDynamicNextState() {
        // 平账
        reconciliationService.generate("BILL-DYN-1", "BOC", "DAILY", "ACC-A",
                new BigDecimal("500"), new BigDecimal("500"));
        reconciliationService.startCompare("BILL-DYN-1");
        reconciliationService.finishCompare("BILL-DYN-1");
        assertEquals("金额相等应流转到 CONFIRMED", ReconciliationStateEnum.CONFIRMED.name(),
                reconciliationService.getBill("BILL-DYN-1").getOrderState());

        // 差异
        reconciliationService.generate("BILL-DYN-2", "BOC", "DAILY", "ACC-B",
                new BigDecimal("500"), new BigDecimal("400"));
        reconciliationService.startCompare("BILL-DYN-2");
        reconciliationService.finishCompare("BILL-DYN-2");
        assertEquals("金额不等应流转到 DIFFERENT", ReconciliationStateEnum.DIFFERENT.name(),
                reconciliationService.getBill("BILL-DYN-2").getOrderState());
        assertEquals("差异笔数应为 1", 1, reconciliationService.getBill("BILL-DYN-2").getDifferenceCount());
    }

    /** 4. 乐观锁：事件携带期望状态与当前状态不一致时抛 ORDER_STATE_NOT_MATCH */
    @Test
    public void testOptimisticLockStateMismatch() {
        ReconciliationBill bill = new ReconciliationBill("BILL-LOCK-001", "GENERATED", "ICBC", "DAILY");
        bill.setAccountNo("ACC-LOCK");
        // 事件期望状态是 COMPARING，但单据当前是 GENERATED
        ReconciliationEvent event = ReconciliationEvent.of("START_COMPARE", "BILL-LOCK-001", "COMPARING");
        try {
            orderFsmEngine.sendEvent(event, bill);
            fail("状态不匹配应抛出 FsmException");
        } catch (FsmException e) {
            assertEquals(ErrorCodeEnum.ORDER_STATE_NOT_MATCH, e.getErrCode());
        } catch (Exception e) {
            fail("应抛出 FsmException，实际: " + e.getClass());
        }
    }

    /** 5. 未找到处理器：非法 state+event 组合抛 NOT_FOUND_PROCESSOR */
    @Test
    public void testProcessorNotFound() {
        ReconciliationBill bill = new ReconciliationBill("BILL-NF-001", "UNKNOWN_STATE", "ICBC", "DAILY");
        ReconciliationEvent event = ReconciliationEvent.of("BAD_EVENT", "BILL-NF-001", "UNKNOWN_STATE");
        try {
            orderFsmEngine.sendEvent(event, bill);
            fail("未找到处理器应抛出 FsmException");
        } catch (FsmException e) {
            assertEquals(ErrorCodeEnum.NOT_FOUND_PROCESSOR, e.getErrCode());
        } catch (Exception e) {
            fail("应抛出 FsmException，实际: " + e.getClass());
        }
    }

    /** 6. 插件机制修复实证：插件既被注册进容器，又在流转中被真实执行 */
    @Test
    public void testPluginMechanismFixed() {
        List<PluginHandler> handlers = pluginRegistry.acquirePluginHandler(
                "COMPARING", "FINISH_COMPARE", "ICBC", "DAILY");
        assertFalse("插件应能被 DefaultPluginRegistry 装载（注册条件已修复）", handlers.isEmpty());

        reconciliationService.generate("BILL-PLG-001", "ICBC", "DAILY", "ACC-P",
                new BigDecimal("100"), new BigDecimal("100"));
        reconciliationService.startCompare("BILL-PLG-001");
        reconciliationService.finishCompare("BILL-PLG-001");
        assertEquals("插件 action 应被真实执行", 1, AuditLogPluginHandler.getExecutedCount());
    }

    /** 7. 释放逻辑反转修复实证：needRelease=true 的 checker 真被 release */
    @Test
    public void testReleaseLogicFixed() {
        assertEquals("初始释放计数应为 0", 0, RiskChecker.getReleaseCount());
        reconciliationService.generate("BILL-REL-001", "ICBC", "DAILY", "ACC-R",
                new BigDecimal("100"), new BigDecimal("100"));
        // startCompare 挂载了 needRelease=true 的 RiskChecker 作为 asyncChecker
        reconciliationService.startCompare("BILL-REL-001");
        assertTrue("needRelease 的 checker 应被 release（释放逻辑未反转）", RiskChecker.getReleaseCount() >= 1);
    }

    /** 8. 校验失败：对账单号为空时参数校验器拦截（引擎返回失败结果） */
    @Test
    public void testParamCheckFailure() throws Exception {
        ReconciliationBill bill = new ReconciliationBill("", "INIT", "ICBC", "DAILY");
        ReconciliationEvent event = ReconciliationEvent.generate("", "ICBC", "DAILY", bill);
        ServiceResult<String, com.lsd.reconciliation.context.ReconciliationContext> result =
                orderFsmEngine.sendEvent(event, bill);
        assertFalse("空单号应校验失败", result.isSuccess());
        assertTrue("失败信息应包含单号校验提示", result.getMsg().contains("单号"));
    }
}
