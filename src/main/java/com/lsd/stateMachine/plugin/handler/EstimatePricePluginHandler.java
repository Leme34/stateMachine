package com.lsd.stateMachine.plugin.handler;

import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.checker.Checker;
import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.plugin.annotation.ProcessorPlugin;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.OrderEventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 预估价插件
 */
@ProcessorPlugin(state = OrderStateEnum.INIT, event = OrderEventEnum.CREATE, bizCode = "BUSINESS")
@Component
public class EstimatePricePluginHandler implements PluginHandler<String, CreateOrderContext> {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public PluginHandlerable getPluginHandlerable(StateContext<CreateOrderContext> context) {
        return new PluginHandlerable() {
            @Override
            public List<PluginHandler> getSyncPluginHandler() {
                EstimatePricePluginHandler estimatePricePluginHandler = (EstimatePricePluginHandler) applicationContext.getBean("estimatePricePluginHandler");
                return Collections.singletonList(estimatePricePluginHandler);
            }
        };
    }

    @Override
    public ServiceResult action(StateContext<CreateOrderContext> context) throws Exception {
//        String price = priceSerive.getPrice();
        String price = "";
        context.getContext().setEstimatePriceInfo(price);
        return new ServiceResult();
    }

}
