package com.lsd.stateMachine.annotation.processor;

import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.enums.OrderEventEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 状态机引擎的处理器注解标识
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface OrderProcessor {
    /**
     * 指定状态，state不能同时存在
     */
    OrderStateEnum[] state() default {};

    /**
     * 业务
     */
    String[] bizCode() default {};

    /**
     * 场景
     */
    String[] sceneId() default {};

    /**
     * 订单操作事件
     */
    OrderEventEnum event();
}
