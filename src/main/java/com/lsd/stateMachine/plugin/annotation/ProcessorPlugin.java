package com.lsd.stateMachine.plugin.annotation;

import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.enums.OrderEventEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 插件注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface ProcessorPlugin {
    /**
     * 指定状态，state不能同时存在
     */
    OrderStateEnum[] state() default {};

    /**
     * 订单操作事件
     */
    OrderEventEnum event();

    /**
     * 业务
     */
    String[] bizCode() default {};

    /**
     * 场景
     */
    String[] sceneId() default {};
}
