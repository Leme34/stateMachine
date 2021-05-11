package com.lsd.stateMachine.context;

import com.lsd.stateMachine.dto.FsmOrder;
import com.lsd.stateMachine.event.OrderStateEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上下文
 * <p>
 * Context对象中一共有三类对象，
 * （1）订单的基本信息（订单ID、状态、业务属性、场景属性）
 * （2）事件对象（其参数基本就是状态迁移行为的入参）
 * （3）具体处理器决定的泛型类。
 * 一般要将数据在多个方法中进行传递有两种方案：一个是包装使用ThreadLocal、每个方法都可以对当前ThreadLocal进行赋值和取值；
 * 另一种是使用一个上下文Context对象做为每个方法的入参传递。
 * 这种方案都有一些优缺点，使用ThreadLocal其实是一种"隐式调用"，虽然可以在"随处"进行调用、但是对使用方其实不明显的、在中间件中会大量使用、在开发业务代码中是需要尽量避免的；
 * 而使用Context做为参数在方法中进行传递、可以有效的减少"不可知"的问题。
 * <p>
 * 不管是使用ThreadLocal还是Context做为参数传递，对于实际承载的数据载体有两种方案，常见的是使用Map做为载体，业务在使用的时候可以根据需要随意的设置任何kv，但是这种情况对代码的可维护性和可读性是极大的挑战，所以这里使用泛型类来固定数据格式，一个具体的状态处理流程到底需要对哪些数据做传递需要明确定义好。
 * 其实原则是一样的，业务开发尽量用用可见性避免不可知。
 * <p>
 * Created by lsd
 * 2021-05-10 16:55
 */
@NoArgsConstructor
@Data
public class StateContext<C> {
    /**
     * 订单操作事件
     */
    private OrderStateEvent orderStateEvent;
    /**
     * 状态机需要的订单基本信息
     */
    private FsmOrder fsmOrder;
    /**
     * 业务可定义的上下文泛型对象
     */
    private C context;

    public StateContext(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) {
        this.orderStateEvent = orderStateEvent;
        this.fsmOrder = fsmOrder;
    }
}
