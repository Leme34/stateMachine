package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.vo.ServiceResult;

/**
 * 状态迁移动作处理步骤
 * 修复：getNextState 返回 String（原 OrderStateEnum 把框架绑死在订单域）。
 */
public interface StateActionStep<T, C> {
    default void prepare(StateContext<C> context) {
    }

    ServiceResult<T, C> check(StateContext<C> context);

    Checkable getCheckable(StateContext<C> context);

    PluginHandlerable getPluginHandlerable(StateContext<C> context);

    /** 下一个状态（字符串，由业务域自行定义） */
    String getNextState(StateContext<C> context);

    ServiceResult<T, C> action(String nextState, StateContext<C> context) throws Exception;

    ServiceResult<T, C> save(String nextState, StateContext<C> context) throws Exception;

    void after(StateContext<C> context);
}
