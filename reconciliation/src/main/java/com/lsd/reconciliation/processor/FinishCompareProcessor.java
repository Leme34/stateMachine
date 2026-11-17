package com.lsd.reconciliation.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.context.ReconciliationContext;
import com.lsd.reconciliation.enums.ReconciliationStateEnum;
import com.lsd.reconciliation.plugin.AuditLogPluginHandler;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 比对完成：COMPARING --FINISH_COMPARE--> CONFIRMED 或 DIFFERENT（动态下一状态）
 * 同时挂载审计插件，验证 action 与 save 之间的插件执行。
 */
@OrderProcessor(state = "COMPARING", event = "FINISH_COMPARE", bizCode = {"ICBC", "CCB", "BOC"}, sceneId = {"DAILY", "MONTHLY"})
public class FinishCompareProcessor extends AbstractStateProcessor<String, ReconciliationContext> {

    @Resource
    private AuditLogPluginHandler auditLogPluginHandler;

    @Override
    public boolean filter(StateContext<ReconciliationContext> context) {
        return true;
    }

    @Override
    public Checkable getCheckable(StateContext<ReconciliationContext> context) {
        return new Checkable() {}; // 无校验器
    }

    @Override
    public ServiceResult<String, ReconciliationContext> check(StateContext<ReconciliationContext> context) {
        return ServiceResult.ok();
    }

    @Override
    public void prepare(StateContext<ReconciliationContext> context) {
        ReconciliationBill bill = (ReconciliationBill) context.getFsmOrder();
        ReconciliationContext c = context.getContext();
        if (c == null) {
            c = new ReconciliationContext();
            context.setContext(c);
        }
        BigDecimal bank = bill.getBankAmount() == null ? BigDecimal.ZERO : bill.getBankAmount();
        BigDecimal system = bill.getSystemAmount() == null ? BigDecimal.ZERO : bill.getSystemAmount();
        BigDecimal diff = bank.subtract(system).abs();
        c.setDifferenceAmount(diff);
        // 比对结论：差异为 0 视为平账，否则存在差异
        boolean balanced = diff.compareTo(BigDecimal.ZERO) == 0;
        c.setCompareResult(balanced ? "BALANCED" : "DIFFERENT");
        bill.setDifferenceCount(balanced ? 0 : 1);
    }

    /** 动态下一状态：根据 prepare 得到的比对结论决定 */
    @Override
    public String getNextState(StateContext<ReconciliationContext> context) {
        ReconciliationContext c = context.getContext();
        if ("BALANCED".equals(c.getCompareResult())) {
            return ReconciliationStateEnum.CONFIRMED.name();
        }
        return ReconciliationStateEnum.DIFFERENT.name();
    }

    @Override
    public ServiceResult<String, ReconciliationContext> action(String nextState, StateContext<ReconciliationContext> context) {
        return ServiceResult.ok();
    }

    @Override
    public PluginHandlerable getPluginHandlerable(StateContext<ReconciliationContext> context) {
        return new PluginHandlerable() {
            @Override
            public List<PluginHandler> getAsyncPluginHandler() {
                return Arrays.asList(auditLogPluginHandler);
            }
        };
    }

    @Override
    public ServiceResult<String, ReconciliationContext> save(String nextState, StateContext<ReconciliationContext> context) {
        ReconciliationBill bill = (ReconciliationBill) context.getFsmOrder();
        bill.setOrderState(nextState);
        return ServiceResult.ok(bill.getBillNo(), "比对完成，结果：" + context.getContext().getCompareResult());
    }

    @Override
    public void after(StateContext<ReconciliationContext> context) {
    }
}
