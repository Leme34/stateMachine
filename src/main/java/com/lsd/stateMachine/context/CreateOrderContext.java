package com.lsd.stateMachine.context;

import com.lsd.stateMachine.dto.FsmOrder;
import com.lsd.stateMachine.event.OrderStateEvent;
import com.lsd.stateMachine.pojo.OrderInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by lsd
 * 2021-05-10 16:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateOrderContext<T> extends StateContext<T> {
    private String estimatePriceInfo;
    private OrderInfo orderInfo;

    public CreateOrderContext(OrderStateEvent orderStateEvent, FsmOrder fsmOrder) {
        super(orderStateEvent, fsmOrder);
    }

}
