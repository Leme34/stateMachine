package com.lsd.stateMachine.engine;

import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.pojo.SimpleFsmOrder;
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
 * <p>
 * 修复点：
 * 1. newCreate 判断反转——新建时由事件构建初始 FsmOrder，已存在时按 orderId 查库；
 * 2. acquireStateProcess 返回空集合而非 null，用 CollectionUtils.isEmpty 判断；
 * 3. 乐观锁状态校验前置（orderState 不匹配及时报错）。
 */
@Component
public class DefaultOrderFsmEngine implements OrderFsmEngine {

    @Autowired
    private FsmOrderService fsmOrderService;
    @Autowired
    private DefaultStateProcessRegistry stateProcessorRegistry;

    @Override
    public <T, C> ServiceResult<T, C> sendEvent(OrderStateEvent orderStateEvent) throws Exception {
        FsmOrder fsmOrder;
        if (orderStateEvent.newCreate()) {
            // 修复：新建单据时由事件提供（或框架构造）初始 FsmOrder，而不是去库里查一个还不存在的单据
            fsmOrder = orderStateEvent.buildFsmOrder();
            if (fsmOrder == null) {
                fsmOrder = new SimpleFsmOrder(
                        orderStateEvent.getOrderId(),
                        orderStateEvent.getInitState(),
                        orderStateEvent.getBizCode(),
                        orderStateEvent.getSceneId());
            }
        } else {
            // 已存在单据：按 orderId 查库获取当前状态/业务/场景
            fsmOrder = this.fsmOrderService.getFsmOrder(orderStateEvent.getOrderId());
            if (fsmOrder == null) {
                throw new FsmException(ErrorCodeEnum.ORDER_NOT_FOUND);
            }
        }
        return sendEvent(orderStateEvent, fsmOrder);
    }

    @Override
    public <T, C> ServiceResult<T, C> sendEvent(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) throws Exception {
        if (fsmOrder == null) {
            throw new FsmException(ErrorCodeEnum.ORDER_NOT_FOUND);
        }
        // 乐观锁：事件携带期望状态且与当前状态不一致时，拒绝迁移
        String expected = orderStateEvent.orderState();
        if (!Objects.isNull(expected) && !expected.equals(fsmOrder.getOrderState())) {
            throw new FsmException(ErrorCodeEnum.ORDER_STATE_NOT_MATCH);
        }
        StateContext context = this.getStateContext(orderStateEvent, fsmOrder);
        StateProcessor<T, ?> stateProcessor = this.getStateProcessor(context);
        return stateProcessor.action(context);
    }

    /**
     * 根据 state+event+bizCode+sceneId + filter 获取唯一的业务处理器
     */
    private <T> StateProcessor<T, ?> getStateProcessor(StateContext<?> context) {
        OrderStateEvent stateEvent = context.getOrderStateEvent();
        FsmOrder fsmOrder = context.getFsmOrder();
        List<AbstractStateProcessor> processorList = stateProcessorRegistry.acquireStateProcess(
                fsmOrder.getOrderState(), stateEvent.getEventType(),
                fsmOrder.bizCode(), fsmOrder.sceneId());
        // 修复：acquireStateProcess 永不返回 null，空集合即未找到
        if (CollectionUtils.isEmpty(processorList)) {
            throw new FsmException(ErrorCodeEnum.NOT_FOUND_PROCESSOR);
        }
        List<AbstractStateProcessor> processorResult = new ArrayList<>(processorList.size());
        for (AbstractStateProcessor processor : processorList) {
            if (processor.filter(context)) {
                processorResult.add(processor);
            }
        }
        if (CollectionUtils.isEmpty(processorResult)) {
            throw new FsmException(ErrorCodeEnum.NOT_FOUND_PROCESSOR);
        }
        if (processorResult.size() > 1) {
            throw new FsmException(ErrorCodeEnum.FOUND_MORE_PROCESSOR);
        }
        return processorResult.get(0);
    }

    private StateContext<?> getStateContext(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) {
        return new StateContext(orderStateEvent, fsmOrder);
    }
}
