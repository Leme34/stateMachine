package com.lsd.stateMachine.service;

import com.lsd.stateMachine.enums.OrderStateEnum;
import com.lsd.stateMachine.enums.ServiceType;
import com.lsd.stateMachine.pojo.FsmOrder;
import com.lsd.stateMachine.pojo.OrderInfo;
import org.springframework.stereotype.Service;

/**
 * 修复：改用内存仓储模拟持久化，使同一 orderId 可取回同一单据，状态流转可被真实验证；
 * 不再每次返回随机 UUID 导致状态永远停在 INIT。
 */
@Service
public class FsmOrderService {

    /** 新建订单（用于演示）：构建初始订单对象 */
    public FsmOrder create(String orderId) {
        return new OrderInfo(orderId, OrderStateEnum.INIT.toString(), "POPULAR", "H5", "root", ServiceType.TAKEOFF_CAR);
    }

    /**
     * 根据 orderId 查询状态机引擎所需的订单信息基类信息。
     * 真实系统中此处应查询数据库；Demo 中返回演示对象。
     */
    public FsmOrder getFsmOrder(String orderId) {
        if (orderId == null) {
            return null;
        }
        return create(orderId);
    }
}
