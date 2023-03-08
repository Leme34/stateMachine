package com.lsd.stateMachine.checker;

import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.StateContext;
import org.springframework.stereotype.Component;

/**
 * Created by lsd
 * 2021-05-11 10:16
 */
@Component
public class UnfinshChecker implements Checker{
    @Override
    public ServiceResult check(StateContext context) {
        return null;
    }
}
