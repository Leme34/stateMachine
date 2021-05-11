package com.lsd.stateMachine.event;

/**
 * Created by lsd
 * 2021-05-11 10:13
 */
public class CreateEvent implements OrderStateEvent{
    @Override
    public String getEventType() {
        return null;
    }

    @Override
    public String getOrderId() {
        return null;
    }

    @Override
    public boolean newCreate() {
        return false;
    }
}
