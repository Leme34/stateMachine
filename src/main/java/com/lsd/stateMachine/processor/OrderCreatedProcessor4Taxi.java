package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.enums.OrderEventEnum;
import org.springframework.stereotype.Component;

/**
 * 当发现新增一个状态不同维度的处理流程，和当前已存在的一个处理器大部分逻辑是相同的，
 * 此时就可以使新写的这个处理器B继承已存在的处理器A，只需要让处理器B覆写A中不同方法逻辑、实现差异逻辑的替换。
 * 这种方案比较好理解，但是需要处理器A已经规划好一些可以扩展的点、其他处理器可以基于这些扩展点进行覆写替换。
 * 当然更好的方案其实是，先实现一个默认的处理器，把所有的标准处理流程和可扩展点进行封装实现、其他处理器进行继承、覆写、替换就好。
 */
@OrderProcessor(state = OrderStateEnum.INIT, event = OrderEventEnum.CREATE, bizCode = "TAXI")
@Component
public class OrderCreatedProcessor4Taxi extends OrderCreatedProcessor {

    @Override
    protected String doPromotion() {
        return "taxt1";
    }

}
