package com.lsd.reconciliation.checker;

import com.lsd.stateMachine.checker.Checker;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.pojo.ReconciliationBill;
import org.springframework.stereotype.Component;

/** 同步校验器：对账账户与账号信息合法性 */
@Component
public class AccountChecker implements Checker {

    @Override
    public ServiceResult check(StateContext context) {
        FsmOrder order = context.getFsmOrder();
        if (order instanceof ReconciliationBill) {
            ReconciliationBill bill = (ReconciliationBill) order;
            if (bill.getAccountNo() == null || bill.getAccountNo().trim().isEmpty()) {
                return ServiceResult.fail("对账账号不能为空");
            }
        }
        return ServiceResult.ok();
    }

    @Override
    public int order() {
        return 2;
    }
}
