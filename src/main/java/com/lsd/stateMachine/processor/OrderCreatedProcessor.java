package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.checker.*;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.OrderEventEnum;
import com.lsd.stateMachine.enums.OrderStateEnum;
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

@Slf4j
@OrderProcessor(state = OrderStateEnum.INIT, bizCode = {"CHEAP", "POPULAR"}, sceneId = "H5", event = OrderEventEnum.CREATE)
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

    /**
     * 订单创建处理器的校验责任链
     */
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
        return null;
    }

    @Override
    public ServiceResult<String, CreateOrderContext> action(String nextState, StateContext<CreateOrderContext> context) throws Exception {
        CreateEvent createEvent = (CreateEvent) context.getOrderStateEvent();

        // 促销信息信息
        String promtionInfo = this.doPromotion();

        // 订单创建业务处理逻辑...

        ServiceResult<String, CreateOrderContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    /**
     * 促销相关扩展点
     */
    protected String doPromotion() {
        return "1";
    }

    @Override
    public OrderStateEnum getNextState(StateContext<CreateOrderContext> context) {
        // if (context.getOrderStateEvent().getEventType().equals("xxx")) {
        //     return OrderStateEnum.INIT;
        //  }
        return OrderStateEnum.NEW;
    }

    @Override
    public ServiceResult<String, CreateOrderContext> save(String nextState, StateContext<CreateOrderContext> context) throws Exception {
        OrderInfo orderInfo = (OrderInfo) context.getFsmOrder();
        // 更新状态
        orderInfo.setOrderState(nextState);
        // 持久化
//        this.updateOrderInfo(orderInfo);
        log.info("save BUSINESS order success, userId:{}, orderId:{}", orderInfo.getUserId(), orderInfo.getOrderId());
        return new ServiceResult<>(orderInfo.getOrderId(), context.getContext(), "business下单成功", true);
    }

    @Override
    public void after(StateContext<CreateOrderContext> context) {

    }

    @Override
    public boolean filter(StateContext<CreateOrderContext> context) {
        OrderInfo orderInfo = (OrderInfo) context.getFsmOrder();
        context.setContext(new CreateOrderContext<>("estimatePriceInfo"));
        if (orderInfo.getServiceType() == ServiceType.TAKEOFF_CAR) {
            return true;
        }
        return false;
    }

    @Override
    public PluginHandlerable getPluginHandlerable(StateContext<CreateOrderContext> context) {
        return new PluginHandlerable() {
            @Override
            public List<PluginHandler> getSyncPluginHandler() {
                return Arrays.asList(estimatePricePluginHandler);
            }

            @Override
            public List<PluginHandler> getAsyncPluginHandler() {
                return Arrays.asList(estimatePricePluginHandler);
            }
        };
    }
}
