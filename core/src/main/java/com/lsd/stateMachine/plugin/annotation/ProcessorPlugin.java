package com.lsd.stateMachine.plugin.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 插件注解
 * 修复：state / event 由枚举改为 String，支持多业务域复用。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface ProcessorPlugin {
    /** 指定状态，可多个 */
    String[] state() default {};

    /** 订单操作事件 */
    String event();

    /** 业务编码 */
    String[] bizCode() default {};

    /** 场景编码 */
    String[] sceneId() default {};
}
