package com.lsd.stateMachine.checker;

import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.StateContext;
import org.springframework.stereotype.Component;

/** 修复：返回 null 会导致并行校验 NPE，改返回成功结果 */
@Component
public class UserChecker implements Checker {
    @Override
    public ServiceResult check(StateContext context) {
        return ServiceResult.ok();
    }
}
