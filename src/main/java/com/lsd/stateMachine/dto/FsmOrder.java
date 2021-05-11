package com.lsd.stateMachine.dto;

/**
 * 状态机引擎所需的订单信息基类信息
 */
public interface FsmOrder {
    /**
     * 订单ID
     */
    String getOrderId();

    /**
     * 订单状态
     */
    String getOrderState();

    /**
     * 订单的业务属性
     */
    String bizCode();

    /**
     * 订单的场景属性
     */
    String sceneId();
}
