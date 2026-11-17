package com.lsd.stateMachine.annotation.processor;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 状态机引擎的处理器注解标识
 * <p>
 * 修复：state / event 由枚举改为 String。原实现写死为 OrderStateEnum / OrderEventEnum，
 * 导致引擎只能服务于订单域；改为 String 后，任意业务域（如银企对账）均可复用同一套引擎，
 * 这也是原文设计初衷（Java 枚举不可继承）。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface OrderProcessor {
    /** 指定状态，可多个 */
    String[] state() default {};

    /** 业务编码（如 CHEAP / POPULAR / TAXI / ICBC） */
    String[] bizCode() default {};

    /** 场景编码（如 H5 / OPEN_API / DAILY） */
    String[] sceneId() default {};

    /** 订单操作事件 */
    String event();
}
