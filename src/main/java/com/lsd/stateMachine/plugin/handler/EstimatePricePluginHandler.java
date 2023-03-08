package com.lsd.stateMachine.plugin.handler;

import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.plugin.annotation.ProcessorPlugin;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.enums.OrderEventEnum;
import org.springframework.stereotype.Component;

/**
 * 预估价插件
 */
@ProcessorPlugin(state = OrderStateEnum.INIT, event = OrderEventEnum.CREATE, bizCode = "BUSINESS")
@Component
public class EstimatePricePluginHandler implements PluginHandler<String, CreateOrderContext> {

    @Override
    public ServiceResult action(StateContext<CreateOrderContext> context) throws Exception {
//        String price = priceSerive.getPrice();
        String price = "";
        context.getContext().setEstimatePriceInfo(price);
        return new ServiceResult();
    }

}
