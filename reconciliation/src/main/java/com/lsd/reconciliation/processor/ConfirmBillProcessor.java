package com.lsd.reconciliation.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.context.ReconciliationContext;
import com.lsd.reconciliation.enums.ReconciliationStateEnum;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import org.springframework.stereotype.Component;

/**
 * 差异确认：DIFFERENT --CONFIRM--> CONFIRMED
 * 演示 filter 二次筛选：仅当对账单确实有差异时才由本处理器处理。
 */
@OrderProcessor(state = "DIFFERENT", event = "CONFIRM", bizCode = {"ICBC", "CCB", "BOC"}, sceneId = {"DAILY", "MONTHLY"})
public class ConfirmBillProcessor extends AbstractStateProcessor<String, ReconciliationContext> {

    @Override
    public boolean filter(StateContext<ReconciliationContext> context) {
        return context.getFsmOrder() instanceof ReconciliationBill
                && ((ReconciliationBill) context.getFsmOrder()).getDifferenceCount() > 0;
    }

    @Override
    public Checkable getCheckable(StateContext<ReconciliationContext> context) {
        return new Checkable() {};
    }

    @Override
    public ServiceResult<String, ReconciliationContext> check(StateContext<ReconciliationContext> context) {
        return ServiceResult.ok();
    }

    @Override
    public String getNextState(StateContext<ReconciliationContext> context) {
        return ReconciliationStateEnum.CONFIRMED.name();
    }

    @Override
    public ServiceResult<String, ReconciliationContext> action(String nextState, StateContext<ReconciliationContext> context) {
        // 人工确认差异处理结论
        return ServiceResult.ok();
    }

    @Override
    public PluginHandlerable getPluginHandlerable(StateContext<ReconciliationContext> context) {
        return new PluginHandlerable() {};
    }

    @Override
    public ServiceResult<String, ReconciliationContext> save(String nextState, StateContext<ReconciliationContext> context) {
        ReconciliationBill bill = (ReconciliationBill) context.getFsmOrder();
        bill.setOrderState(nextState);
        bill.setDifferenceCount(0); // 差异已处理
        return ServiceResult.ok(bill.getBillNo(), "差异已确认，对账完成");
    }

    @Override
    public void after(StateContext<ReconciliationContext> context) {
    }
}
