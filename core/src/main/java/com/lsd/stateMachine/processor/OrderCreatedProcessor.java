package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.*;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.ServiceType;
import com.lsd.stateMachine.event.CreateEvent;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.plugin.handler.EstimatePricePluginHandler;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.pojo.OrderInfo;
import com.lsd.stateMachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 订单域示例处理器（state/event 注解已改为 String） */
@Slf4j
@OrderProcessor(state = "INIT", bizCode = {"CHEAP", "POPULAR"}, sceneId = "H5", event = "CREATE")
@Component
public class OrderCreatedProcessor extends AbstractStateProcessor<String, CreateOrderContext> {

    @Resource
    private CreateParamChecker createParamChecker;
    @Resource
    private UserChecker userChecker;
    @Resource
    private UnfinshChecker unfinshChecker;
    @Resource
    private EstimatePricePluginHandler estimatePricePluginHandler;

    @Override
    public Checkable getCheckable(StateContext<CreateOrderContext> context) {
        return new Checkable() {
            @Override
            public List<Checker> getParamChecker() {
                return Arrays.asList(createParamChecker);
            }

            @Override
            public List<Checker> getSyncChecker() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public List<Checker> getAsyncChecker() {
                return Arrays.asList(userChecker, unfinshChecker);
            }
        };
    }

    @Override
    public ServiceResult<String, CreateOrderContext> check(StateContext<CreateOrderContext> context) {
        return ServiceResult.ok();
    }

    @Override
    public ServiceResult<String, CreateOrderContext> action(String nextState, StateContext<CreateOrderContext> context) throws Exception {
        String promotionInfo = this.doPromotion();
        log.debug("order promote info: {}", promotionInfo);
        ServiceResult<String, CreateOrderContext> result = ServiceResult.ok();
        result.setContext(context.getContext());
        return result;
    }

    /** 促销相关扩展点 */
    protected String doPromotion() {
        return "1";
    }

    @Override
    public String getNextState(StateContext<CreateOrderContext> context) {
        return "NEW";
    }

    @Override
    public ServiceResult<String, CreateOrderContext> save(String nextState, StateContext<CreateOrderContext> context) throws Exception {
        OrderInfo orderInfo = (OrderInfo) context.getFsmOrder();
        orderInfo.setOrderState(nextState);
        log.info("save BUSINESS order success, userId:{}, orderId:{}", orderInfo.getUserId(), orderInfo.getOrderId());
        return ServiceResult.ok(orderInfo.getOrderId(), "business下单成功");
    }

    @Override
    public void after(StateContext<CreateOrderContext> context) {
    }

    @Override
    public boolean filter(StateContext<CreateOrderContext> context) {
        OrderInfo orderInfo = (OrderInfo) context.getFsmOrder();
        context.setContext(new CreateOrderContext<>("estimatePriceInfo"));
        return orderInfo.getServiceType() == ServiceType.TAKEOFF_CAR;
    }

    @Override
    public PluginHandlerable getPluginHandlerable(StateContext<CreateOrderContext> context) {
        return new PluginHandlerable() {
            @Override
            public List<PluginHandler> getSyncPluginHandler() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public List<PluginHandler> getAsyncPluginHandler() {
                return Arrays.asList(estimatePricePluginHandler);
            }
        };
    }
}
