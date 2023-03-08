package com.lsd.stateMachine.service;

import com.lsd.stateMachine.enums.ServiceType;
import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.pojo.OrderInfo;
import com.lsd.stateMachine.enums.OrderStateEnum;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by lsd
 * 2021-05-11 11:31
 */
@Service
public class FsmOrderService {

    /**
     * 根据 orderId 查询状态机引擎所需的订单信息基类信息
     */
    public FsmOrder getFsmOrder(String orderId) {
        return new OrderInfo(UUID.randomUUID().toString(), OrderStateEnum.INIT.toString(), "POPULAR", "H5", "root", ServiceType.TAKEOFF_CAR);
    }


}
