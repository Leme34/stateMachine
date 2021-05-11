package com.lsd.stateMachine.pojo;

import com.lsd.stateMachine.enums.ServiceType;
import lombok.Data;

/**
 * Created by lsd
 * 2021-05-11 09:31
 */
@Data
public class OrderInfo {
    private String orderState;
    private String orderId;
    private String userId;
    private ServiceType serviceType;
}
