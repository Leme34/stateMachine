package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import org.springframework.stereotype.Component;

/** 差异化处理器：继承标准处理器，仅覆写扩展点（注解已改为 String） */
@OrderProcessor(state = "INIT", event = "CREATE", bizCode = "TAXI")
@Component
public class OrderCreatedProcessor4Taxi extends OrderCreatedProcessor {

    @Override
    protected String doPromotion() {
        return "taxi1";
    }
}
