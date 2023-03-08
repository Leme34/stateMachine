package com.lsd.stateMachine.event;

import com.lsd.stateMachine.enums.OrderEventEnum;

import java.util.UUID;

/**
 * Created by lsd
 * 2021-05-11 10:13
 */
public class CreateEvent implements OrderStateEvent{
    @Override
    public String getEventType() {
        return OrderEventEnum.CREATE.toString();
    }

    @Override
    public String getOrderId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean newCreate() {
        return true;
    }
}
