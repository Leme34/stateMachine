package com.lsd.stateMachine;

import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.engine.OrderFsmEngine;
import com.lsd.stateMachine.event.CreateEvent;
import com.lsd.stateMachine.vo.ServiceResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author synda
 * @date 2023/2/24
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultOrderFsmEngineTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderFsmEngineTest.class);

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @Test
    public void sendEventTest() {
        CreateEvent createEvent = new CreateEvent();
        try {
            ServiceResult<String, CreateOrderContext> result = orderFsmEngine.sendEvent(createEvent);
            logger.info("result=" + result);
        } catch (Exception e) {
            logger.error("failed", e);
        }
    }

}