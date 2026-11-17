package com.lsd.reconciliation.service;

import com.lsd.stateMachine.engine.OrderFsmEngine;
import com.lsd.stateMachine.enums.ErrorCodeEnum;
import com.lsd.stateMachine.exception.FsmException;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.context.ReconciliationContext;
import com.lsd.reconciliation.enums.ReconciliationStateEnum;
import com.lsd.reconciliation.event.ReconciliationEvent;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 银企对账编排服务：对账单状态流转的领域入口。
 *
 * <p>职责：
 * <ol>
 *   <li>维护对账单仓储（Demo 用内存 Map 模拟 DB，真实系统应替换成 DAO）；</li>
 *   <li>将外部请求封装成领域事件，交给状态机引擎驱动状态迁移；</li>
 *   <li>使用两参 {@code sendEvent(event, fsmOrder)} 显式传入对账单对象，
 *       避免核心框架默认的 {@code FsmOrderService}（返回订单域 OrderInfo）与对账域冲突。</li>
 * </ol>
 *
 * <p>支持状态链：
 * <pre>
 *   INIT --GENERATE--> GENERATED --START_COMPARE--> COMPARING
 *        --FINISH_COMPARE--> (金额相等 ? CONFIRMED : DIFFERENT)
 *        --CONFIRM--> CONFIRMED --CLOSE--> CLOSED
 * </pre>
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    /** 对账单仓储（Demo 内存实现） */
    private final Map<String, ReconciliationBill> repository = new ConcurrentHashMap<>();

    /**
     * 生成对账单：INIT --GENERATE--> GENERATED
     */
    public ServiceResult<String, ReconciliationContext> generate(String billNo, String bankCode, String sceneId,
                                                                  String accountNo, BigDecimal bankAmount,
                                                                  BigDecimal systemAmount) {
        ReconciliationBill bill = new ReconciliationBill(billNo, ReconciliationStateEnum.INIT.name(), bankCode, sceneId);
        bill.setAccountNo(accountNo);
        bill.setBankAmount(bankAmount);
        bill.setSystemAmount(systemAmount);
        repository.put(billNo, bill);
        ReconciliationEvent event = ReconciliationEvent.generate(billNo, bankCode, sceneId, bill);
        return fire(event, bill, "生成对账单");
    }

    /** 开始比对：GENERATED --START_COMPARE--> COMPARING */
    public ServiceResult<String, ReconciliationContext> startCompare(String billNo) {
        return transition(billNo, ReconciliationEventEnumHolder.START_COMPARE,
                ReconciliationStateEnum.GENERATED, "开始比对");
    }

    /** 比对完成：COMPARING --FINISH_COMPARE--> CONFIRMED / DIFFERENT（动态下一状态） */
    public ServiceResult<String, ReconciliationContext> finishCompare(String billNo) {
        return transition(billNo, ReconciliationEventEnumHolder.FINISH_COMPARE,
                ReconciliationStateEnum.COMPARING, "比对完成");
    }

    /** 差异确认：DIFFERENT --CONFIRM--> CONFIRMED */
    public ServiceResult<String, ReconciliationContext> confirm(String billNo) {
        return transition(billNo, ReconciliationEventEnumHolder.CONFIRM,
                ReconciliationStateEnum.DIFFERENT, "差异确认");
    }

    /** 关闭对账单：CONFIRMED --CLOSE--> CLOSED */
    public ServiceResult<String, ReconciliationContext> close(String billNo) {
        ReconciliationBill bill = require(billNo);
        // 关闭允许从 CONFIRMED 或 INIT 直接进入，用当前状态做乐观锁校验
        ReconciliationEvent event = ReconciliationEvent.of(ReconciliationEventEnumHolder.CLOSE, billNo, bill.getOrderState());
        return fire(event, bill, "关闭对账单");
    }

    /** 一键走完平账链路：生成 → 开始比对 → 比对完成（平账）→ 关闭 */
    public ReconciliationBill runBalancedFlow(String billNo, String bankCode, String sceneId, String accountNo,
                                              BigDecimal amount) {
        generate(billNo, bankCode, sceneId, accountNo, amount, amount);
        startCompare(billNo);
        finishCompare(billNo);
        close(billNo);
        return require(billNo);
    }

    /** 一键走完差异链路：生成 → 开始比对 → 比对完成（差异）→ 差异确认 → 关闭 */
    public ReconciliationBill runDifferenceFlow(String billNo, String bankCode, String sceneId, String accountNo,
                                                BigDecimal bankAmount, BigDecimal systemAmount) {
        generate(billNo, bankCode, sceneId, accountNo, bankAmount, systemAmount);
        startCompare(billNo);
        finishCompare(billNo);
        confirm(billNo);
        close(billNo);
        return require(billNo);
    }

    /** 通用流转：带乐观锁校验（期望当前状态为 expected） */
    private ServiceResult<String, ReconciliationContext> transition(String billNo, String eventType,
                                                                    ReconciliationStateEnum expected, String desc) {
        ReconciliationBill bill = require(billNo);
        ReconciliationEvent event = ReconciliationEvent.of(eventType, billNo, expected.name());
        return fire(event, bill, desc);
    }

    private ServiceResult<String, ReconciliationContext> fire(ReconciliationEvent event, ReconciliationBill bill, String desc) {
        try {
            ServiceResult<String, ReconciliationContext> result = orderFsmEngine.sendEvent(event, bill);
            log.info("[对账] {} 完成, billNo={}, state={}, success={}, msg={}",
                    desc, bill.getBillNo(), bill.getOrderState(), result.isSuccess(), result.getMsg());
            return result;
        } catch (Exception e) {
            log.error("[对账] {} 失败, billNo={}", desc, bill.getBillNo(), e);
            throw new RuntimeException(desc + "失败: " + e.getMessage(), e);
        }
    }

    private ReconciliationBill require(String billNo) {
        ReconciliationBill bill = repository.get(billNo);
        if (bill == null) {
            throw new FsmException(ErrorCodeEnum.ORDER_NOT_FOUND);
        }
        return bill;
    }

    /** 供测试/外部查询当前对账单 */
    public ReconciliationBill getBill(String billNo) {
        return require(billNo);
    }

    /** 事件类型常量（避免与枚举名耦合，便于演示字符串驱动的引擎） */
    private static final class ReconciliationEventEnumHolder {
        static final String START_COMPARE = "START_COMPARE";
        static final String FINISH_COMPARE = "FINISH_COMPARE";
        static final String CONFIRM = "CONFIRM";
        static final String CLOSE = "CLOSE";
    }
}
