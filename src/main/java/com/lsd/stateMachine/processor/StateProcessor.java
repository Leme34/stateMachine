package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.StateContext;

/**
 * 状态机处理器接口
 */
public interface StateProcessor<T,C> {
    /**
     * 执行状态迁移的入口
     */
    ServiceResult<T, C> action(StateContext<C> context) throws Exception;
}
