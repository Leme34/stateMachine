package com.lsd.stateMachine.pojo;

/**
 * 框架内置的通用 FsmOrder 实现。
 * 业务域也可以用自定义类实现 {@link FsmOrder} 接口（如银企对账的对账单）。
 */
public class SimpleFsmOrder implements FsmOrder {

    private final String orderId;
    private String orderState;
    private final String bizCode;
    private final String sceneId;

    public SimpleFsmOrder(String orderId, String orderState, String bizCode, String sceneId) {
        this.orderId = orderId;
        this.orderState = orderState;
        this.bizCode = bizCode;
        this.sceneId = sceneId;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    @Override
    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    @Override
    public String bizCode() {
        return bizCode;
    }

    @Override
    public String sceneId() {
        return sceneId;
    }
}
