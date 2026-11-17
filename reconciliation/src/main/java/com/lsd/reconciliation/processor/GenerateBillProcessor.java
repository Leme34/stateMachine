package com.lsd.reconciliation.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.checker.Checker;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.checker.BillParamChecker;
import com.lsd.reconciliation.context.ReconciliationContext;
import com.lsd.reconciliation.enums.ReconciliationStateEnum;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 生成对账单：INIT --GENERATE--> GENERATED */
@OrderProcessor(state = "INIT", event = "GENERATE", bizCode = {"ICBC", "CCB", "BOC"}, sceneId = {"DAILY", "MONTHLY"})
public class GenerateBillProcessor extends AbstractStateProcessor<String, ReconciliationContext> {

    @Resource
    private BillParamChecker billParamChecker;

    @Override
    public boolean filter(StateContext<ReconciliationContext> context) {
        return context.getFsmOrder() instanceof ReconciliationBill;
    }

    @Override
    public Checkable getCheckable(StateContext<ReconciliationContext> context) {
        return new Checkable() {
            @Override
            public List<Checker> getParamChecker() {
                return Arrays.asList(billParamChecker);
            }
        };
    }

    @Override
    public ServiceResult<String, ReconciliationContext> check(StateContext<ReconciliationContext> context) {
        return ServiceResult.ok();
    }

    @Override
    public void prepare(StateContext<ReconciliationContext> context) {
        context.setContext(new ReconciliationContext());
    }

    @Override
    public String getNextState(StateContext<ReconciliationContext> context) {
        return ReconciliationStateEnum.GENERATED.name();
    }

    @Override
    public ServiceResult<String, ReconciliationContext> action(String nextState, StateContext<ReconciliationContext> context) {
        ReconciliationBill bill = (ReconciliationBill) context.getFsmOrder();
        bill.setOperator("system");
        return ServiceResult.ok();
    }

    @Override
    public PluginHandlerable getPluginHandlerable(StateContext<ReconciliationContext> context) {
        return new PluginHandlerable() {}; // 无插件
    }

    @Override
    public ServiceResult<String, ReconciliationContext> save(String nextState, StateContext<ReconciliationContext> context) {
        ReconciliationBill bill = (ReconciliationBill) context.getFsmOrder();
        bill.setOrderState(nextState);
        return ServiceResult.ok(bill.getBillNo(), "对账单已生成");
    }

    @Override
    public void after(StateContext<ReconciliationContext> context) {
        // 可扩展：发送消息通知下游
    }
}
