package com.lsd.stateMachine.plugin.handler;

import com.lsd.stateMachine.plugin.annotation.ProcessorPlugin;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.CreateOrderContext;
import com.lsd.stateMachine.context.StateContext;
import org.springframework.stereotype.Component;

/** 预估价插件（注解改 String；返回成功结果） */
@ProcessorPlugin(state = "INIT", event = "CREATE", bizCode = "BUSINESS")
@Component
public class EstimatePricePluginHandler implements PluginHandler<String, CreateOrderContext> {

    @Override
    public ServiceResult action(StateContext<CreateOrderContext> context) throws Exception {
        String price = "estimated";
        if (context.getContext() != null) {
            context.getContext().setEstimatePriceInfo(price);
        }
        return ServiceResult.ok();
    }
}
