package com.lsd.stateMachine.event;

import com.lsd.stateMachine.enums.OrderEventEnum;
import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.enums.ServiceType;
import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.pojo.OrderInfo;

import java.util.UUID;

/** 订单创建事件（新建场景：提供业务/场景/初始状态与初始 FsmOrder） */
public class CreateEvent implements OrderStateEvent {

    private final String orderId;

    public CreateEvent() {
        this(UUID.randomUUID().toString());
    }

    public CreateEvent(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String getEventType() {
        return OrderEventEnum.CREATE.toString();
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    @Override
    public boolean newCreate() {
        return true;
    }

    @Override
    public String getBizCode() {
        return "POPULAR";
    }

    @Override
    public String getSceneId() {
        return "H5";
    }

    @Override
    public String getInitState() {
        return OrderStateEnum.INIT.toString();
    }

    @Override
    public FsmOrder buildFsmOrder() {
        return new OrderInfo(orderId, getInitState(), getBizCode(), getSceneId(), "root", ServiceType.TAKEOFF_CAR);
    }
}
