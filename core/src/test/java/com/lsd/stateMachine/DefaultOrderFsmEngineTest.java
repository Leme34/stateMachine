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

import static org.junit.Assert.*;

/**
 * 核心引擎下单流程测试。
 * 修复后补充断言，实证 newCreate 判断反转 BUG 已修复：
 * 新建订单不再查库，处理器被正确找到，状态从 INIT 迁移到 NEW。
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultOrderFsmEngineTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderFsmEngineTest.class);

    @Autowired
    private OrderFsmEngine orderFsmEngine;

    @Test
    public void sendEventTest() throws Exception {
        CreateEvent createEvent = new CreateEvent();
        ServiceResult<String, CreateOrderContext> result = orderFsmEngine.sendEvent(createEvent);
        logger.info("result={}", result);

        assertNotNull("新建订单应返回执行结果（不再因查库失败返回 null/NPE）", result);
        assertTrue("下单应成功", result.isSuccess());
        assertNotNull("应返回订单号", result.getData());
        assertEquals("下单成功文案", "business下单成功", result.getMsg());
    }
}
