package com.lsd.stateMachine.engine;

import com.lsd.stateMachine.dto.FsmOrder;
import com.lsd.stateMachine.event.OrderStateEvent;
import com.lsd.stateMachine.vo.ServiceResult;

/**
 * （2）状态机引擎运行时阶段：状态机执行引擎接口
 * <p>
 * 经过初始化之后，所有的状态处理器 processor 都被装载到容器。
 * 在运行时，通过一个入口来发起对状态机的调用，方法的主要参数是操作事件（event）和业务入参，
 * 如果是新创建订单请求，则需要携带业务（bizCode）和场景（sceneId）信息；
 * 如果是已存在订单的更新，状态机引擎会根据 oderId 自动获取业务（bizCode）、场景（sceneId）和当前状态（state）。
 * 之后引擎根据 state+event+bizCode+sceneId 从状态处理器容器中获取到对应的具体处理器 processor，从而进行状态迁移处理。
 */
public interface OrderFsmEngine {
    /**
     * 执行状态迁移事件，不携带 FsmOrder 参数，默认会根据 orderId 从 FsmOrderService 接口获取
     */
    <T,C> ServiceResult<T,C> sendEvent(OrderStateEvent orderStateEvent) throws Exception;

    /**
     * 执行状态迁移事件，可携带 FsmOrder 参数
     */
    <T,C> ServiceResult<T,C> sendEvent(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) throws Exception;
}
