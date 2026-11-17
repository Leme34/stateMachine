package com.lsd.stateMachine.pojo;

import com.lsd.stateMachine.enums.ServiceType;
import com.lsd.stateMachine.pojo.FsmOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author synda
 * @date 2023/3/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo implements FsmOrder {
    private String orderId;
    private String orderState;
    private String bizCode;
    private String sceneId;
    private String userId;
    private ServiceType serviceType;

    @Override
    public String bizCode() {
        return bizCode;
    }

    @Override
    public String sceneId() {
        return sceneId;
    }
}
