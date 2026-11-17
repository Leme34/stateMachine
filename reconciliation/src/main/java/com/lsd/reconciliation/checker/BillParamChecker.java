package com.lsd.reconciliation.checker;

import com.lsd.stateMachine.checker.Checker;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.context.ReconciliationContext;
import org.springframework.stereotype.Component;

/** 参数校验器：对账单号不能为空 */
@Component
public class BillParamChecker implements Checker {

    @Override
    public ServiceResult check(StateContext context) {
        FsmOrder order = context.getFsmOrder();
        if (order == null || order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            return ServiceResult.fail("对账单号不能为空");
        }
        return ServiceResult.ok();
    }

    @Override
    public int order() {
        return 1;
    }
}
