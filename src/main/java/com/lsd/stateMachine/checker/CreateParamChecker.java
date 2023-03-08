package com.lsd.stateMachine.checker;

import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.StateContext;
import org.springframework.stereotype.Component;

/**
 * Created by lsd
 * 2021-05-11 10:16
 */
@Component
public class CreateParamChecker implements Checker{
    @Override
    public ServiceResult check(StateContext context) {
        ServiceResult<String,StateContext> result = new ServiceResult();
        result.setContext(context);
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }
}
