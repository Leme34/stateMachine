package com.lsd.stateMachine.engine;

import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.ErrorCodeEnum;
import com.lsd.stateMachine.event.OrderStateEvent;
import com.lsd.stateMachine.exception.FsmException;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
import com.lsd.stateMachine.processor.StateProcessor;
import com.lsd.stateMachine.service.FsmOrderService;
import com.lsd.stateMachine.vo.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * （2）状态机引擎运行时阶段：状态机执行引擎实现
 */
@Component
public class DefaultOrderFsmEngine implements OrderFsmEngine {

    @Autowired
    private FsmOrderService fsmOrderService;
    @Autowired
    private DefaultStateProcessRegistry stateProcessorRegistry;

    @Override
    public <T, C> ServiceResult<T, C> sendEvent(OrderStateEvent orderStateEvent) throws Exception {
        FsmOrder fsmOrder = null;
        if (orderStateEvent.newCreate()) {
            fsmOrder = this.fsmOrderService.getFsmOrder(orderStateEvent.getOrderId());
            if (fsmOrder == null) {
                throw new FsmException(ErrorCodeEnum.ORDER_NOT_FOUND);
            }
        }
        return sendEvent(orderStateEvent, fsmOrder);
    }

    @Override
    public <T, C> ServiceResult<T, C> sendEvent(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) throws Exception {
        // 构造当前事件上下文
        StateContext context = this.getStateContext(orderStateEvent, fsmOrder);
        // 获取当前事件处理器
        StateProcessor<T, ?> stateProcessor = this.getStateProcessor(context);
        // 执行处理逻辑
        return stateProcessor.action(context);
    }

    /**
     * 获取当前事件处理器
     */
    private <T> StateProcessor<T, ?> getStateProcessor(StateContext<?> context) {
        OrderStateEvent stateEvent = context.getOrderStateEvent();
        FsmOrder fsmOrder = context.getFsmOrder();
        // 根据 state+event+bizCode+sceneId 信息获取所对应的业务处理器集合
        List<AbstractStateProcessor> processorList = stateProcessorRegistry.acquireStateProcess(
                fsmOrder.getOrderState(), stateEvent.getEventType(),
                fsmOrder.bizCode(), fsmOrder.sceneId());
        if (processorList == null) {
            // 订单状态发生改变
            if (!Objects.isNull(stateEvent.orderState()) &&
                    !stateEvent.orderState().equals(fsmOrder.getOrderState())) {
                throw new FsmException(ErrorCodeEnum.ORDER_STATE_NOT_MATCH);
            }
            throw new FsmException(ErrorCodeEnum.NOT_FOUND_PROCESSOR);
        }
        List<AbstractStateProcessor> processorResult = new ArrayList<>(processorList.size());
        // 根据上下文获取唯一的业务处理器
        // 因为有可能根据 state+event+bizCode+sceneId 信息获取到的是多个状态处理器processor，有可能确实业务需要单纯依赖bizCode和sceneId两个属性无法有效识别和定位唯一processor，那么我们这里给业务开一个口、由业务决定从多个处理器中选一个适合当前上下文的，具体做法是业务processor通过filter方法根据当前context来判断是否符合调用条件。
        for (AbstractStateProcessor processor : processorList) {
            if (processor.filter(context)) {
                processorResult.add(processor);
            }
        }
        //如果最终经过业务filter之后，还是有多个状态处理器符合条件，那么这里只能抛异常处理了。这个需要在开发时，对状态和多维度处理器有详细规划。
        if (CollectionUtils.isEmpty(processorResult)) {
            throw new FsmException(ErrorCodeEnum.NOT_FOUND_PROCESSOR);
        }
        if (processorResult.size() > 1) {
            throw new FsmException(ErrorCodeEnum.FOUND_MORE_PROCESSOR);
        }
        return processorResult.get(0);
    }

    /**
     * 构造当前事件上下文
     */
    private StateContext<?> getStateContext(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) {
        return (StateContext<?>) new StateContext(orderStateEvent, fsmOrder);
    }
}
