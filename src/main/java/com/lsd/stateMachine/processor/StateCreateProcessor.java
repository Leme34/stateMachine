package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.OrderEventEnum;
import org.springframework.stereotype.Component;

/**
 * 创建订单状态对应的状态处理器
 */
@OrderProcessor(state = OrderStateEnum.INIT, bizCode = {"CHEAP", "POPULAR"}, sceneId = "H5", event = OrderEventEnum.CREATE)
@Component
public class StateCreateProcessor extends AbstractStateProcessor<String, CreateOrderContext> {

    @Override
    public void prepare(StateContext<CreateOrderContext> context) {

    }

    @Override
    public ServiceResult<String, CreateOrderContext> check(StateContext<CreateOrderContext> context) {
        return null;
    }

    @Override
    public Checkable getCheckable(StateContext<CreateOrderContext> context) {
        return null;
    }

    @Override
    public OrderStateEnum getNextState(StateContext<CreateOrderContext> context) {
        return null;
    }

    @Override
    public ServiceResult<String, CreateOrderContext> action(String nextState, StateContext<CreateOrderContext> context) throws Exception {
        return null;
    }

    @Override
    public ServiceResult<String, CreateOrderContext> save(String nextState, StateContext<CreateOrderContext> context) throws Exception {
        return null;
    }

    @Override
    public void after(StateContext<CreateOrderContext> context) {

    }

    @Override
    public boolean filter(StateContext<CreateOrderContext> context) {
        return false;
    }
}
