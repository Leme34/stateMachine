package com.lsd.reconciliation.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.checker.Checker;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.checker.AccountChecker;
import com.lsd.reconciliation.checker.RiskChecker;
import com.lsd.reconciliation.context.ReconciliationContext;
import com.lsd.reconciliation.enums.ReconciliationStateEnum;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** 开始比对：GENERATED --START_COMPARE--> COMPARING */
@OrderProcessor(state = "GENERATED", event = "START_COMPARE", bizCode = {"ICBC", "CCB", "BOC"}, sceneId = {"DAILY", "MONTHLY"})
public class StartCompareProcessor extends AbstractStateProcessor<String, ReconciliationContext> {

    @Resource
    private AccountChecker accountChecker;
    @Resource
    private RiskChecker riskChecker;

    @Override
    public boolean filter(StateContext<ReconciliationContext> context) {
        return true;
    }

    @Override
    public Checkable getCheckable(StateContext<ReconciliationContext> context) {
        return new Checkable() {
            @Override
            public List<Checker> getSyncChecker() {
                return Arrays.asList(accountChecker);
            }

            @Override
            public List<Checker> getAsyncChecker() {
                return Arrays.asList(riskChecker);
            }
        };
    }

    @Override
    public ServiceResult<String, ReconciliationContext> check(StateContext<ReconciliationContext> context) {
        return ServiceResult.ok();
    }

    @Override
    public void prepare(StateContext<ReconciliationContext> context) {
        // 数据准备：拉取银行端流水与系统端账务（Demo 中直接取对账单上的金额）
        ReconciliationBill bill = (ReconciliationBill) context.getFsmOrder();
        ReconciliationContext c = context.getContext();
        if (c == null) {
            c = new ReconciliationContext();
            context.setContext(c);
        }
        c.setBankSideTotal(bill.getBankAmount() == null ? BigDecimal.ZERO : bill.getBankAmount());
        c.setSystemSideTotal(bill.getSystemAmount() == null ? BigDecimal.ZERO : bill.getSystemAmount());
    }

    @Override
    public String getNextState(StateContext<ReconciliationContext> context) {
        return ReconciliationStateEnum.COMPARING.name();
    }

    @Override
    public ServiceResult<String, ReconciliationContext> action(String nextState, StateContext<ReconciliationContext> context) {
        // 比对动作在 FinishCompare 完成，这里仅切换到比对中状态
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
        return ServiceResult.ok(bill.getBillNo(), "开始比对");
    }

    @Override
    public void after(StateContext<ReconciliationContext> context) {
    }
}
