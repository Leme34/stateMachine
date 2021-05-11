package com.lsd.stateMachine.enums;

/**
 * Created by lsd
 * 2021-05-11 09:15
 */
public enum OrderStateEnum {
    INIT("初始化"),
    NEW("新建"),
    ;

    OrderStateEnum(String state) {
    }

    @Override
    public String toString() {
        return this.name();
    }
}
