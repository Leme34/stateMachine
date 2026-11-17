package com.lsd.stateMachine.enums;

/**
 * Created by lsd
 * 2021-05-11 09:15
 */
public enum OrderEventEnum {
    CREATE("创建"),
    ;

    OrderEventEnum(String state) {
    }

    @Override
    public String toString() {
        return this.name();
    }

}
