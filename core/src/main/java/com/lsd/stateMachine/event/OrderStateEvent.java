package com.lsd.stateMachine.event;

import com.lsd.stateMachine.pojo.FsmOrder;

/**
 * 订单状态迁移事件
 * 修复：新增 getBizCode / getSceneId 默认方法，为新建场景提供业务/场景维度信息。
 */
public interface OrderStateEvent {
    /** 订单状态事件（事件类型字符串） */
    String getEventType();

    /** 订单ID */
    String getOrderId();

    /**
     * 如果orderState不为空，则代表只有订单是当前状态才进行迁移（乐观锁校验）
     */
    default String orderState() {
        return null;
    }

    /** 是否要新创建单据：新建时由事件提供 bizCode / sceneId / 初始状态 */
    boolean newCreate();

    /** 新建单据时的业务编码（仅 newCreate=true 时使用） */
    default String getBizCode() {
        return null;
    }

    /** 新建单据时的场景编码（仅 newCreate=true 时使用） */
    default String getSceneId() {
        return null;
    }

    /** 新建单据时的初始状态（仅 newCreate=true 时使用） */
    default String getInitState() {
        return null;
    }

    /**
     * 新建单据时，由事件提供初始 FsmOrder（可选）。
     * 返回 null 时框架用 orderId/getInitState/getBizCode/getSceneId 构造 SimpleFsmOrder。
     */
    default FsmOrder buildFsmOrder() {
        return null;
    }
}
