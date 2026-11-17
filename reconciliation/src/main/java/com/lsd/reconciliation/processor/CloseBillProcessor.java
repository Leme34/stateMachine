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
 * 关闭对账单：CONFIRMED --CLOSE--> CLOSED
 * 演示同一处理器标注多个 state（INIT 也允许直接作废关闭）。
 */
@OrderProcessor(state = {"CONFIRMED", "INIT"},
        event = "CLOSE", bizCode = {"ICBC", "CCB", "BOC"}, sceneId = {"DAILY", "MONTHLY"})
public class CloseBillProcessor extends AbstractStateProcessor<String, ReconciliationContext> {

    @Override
    public boolean filter(StateContext<ReconciliationContext> context) {
        return true;
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
        return ReconciliationStateEnum.CLOSED.name();
    }

    @Override
    public ServiceResult<String, ReconciliationContext> action(String nextState, StateContext<ReconciliationContext> context) {
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
        return ServiceResult.ok(bill.getBillNo(), "对账单已关闭");
    }

    @Override
    public void after(StateContext<ReconciliationContext> context) {
    }
}
