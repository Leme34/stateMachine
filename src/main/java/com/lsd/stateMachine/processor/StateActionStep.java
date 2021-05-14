package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.vo.ServiceResult;

/**
 * 状态迁移动作处理步骤
 */
public interface StateActionStep<T, C> {
    /**
     * 准备数据
     */
    default void prepare(StateContext<C> context) {
    }

    /**
     * 校验
     */
    ServiceResult<T, C> check(StateContext<C> context);

    /**
     * 处理器的校验责任链
     */
    Checkable getCheckable(StateContext<C> context);

    /**
     * 处理器的插件责任链
     */
    PluginHandlerable getPluginHandlerable(StateContext<C> context);

    /**
     * 获取当前状态处理器处理完毕后，所处于的下一个状态，这里把下一个状态的判定交由业务逻辑根据上下文对象自己来判断。
     */
    OrderStateEnum getNextState(StateContext<C> context);

    /**
     * 状态动作方法，主要状态迁移逻辑
     */
    ServiceResult<T, C> action(String nextState, StateContext<C> context) throws Exception;

    /**
     * 状态数据持久化
     */
    ServiceResult<T, C> save(String nextState, StateContext<C> context) throws Exception;

    /**
     * 状态迁移成功，持久化后执行的后续处理
     */
    void after(StateContext<C> context);
}
