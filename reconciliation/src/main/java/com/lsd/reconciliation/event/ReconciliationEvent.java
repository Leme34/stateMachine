package com.lsd.reconciliation.event;

import com.lsd.stateMachine.event.OrderStateEvent;
import com.lsd.stateMachine.pojo.FsmOrder;

import java.util.UUID;

/**
 * 通用对账事件。通过事件类型区分动作；新建（GENERATE）时提供初始 FsmOrder。
 */
public class ReconciliationEvent implements OrderStateEvent {

    private final String eventType;
    private final String billNo;
    private String expectedState;     // 乐观锁：期望当前状态
    private FsmOrder fsmOrder;        // 新建时携带初始对账单
    private String bizCode;
    private String sceneId;

    public ReconciliationEvent(String eventType, String billNo) {
        this.eventType = eventType;
        this.billNo = billNo;
    }

    /** 用于新建对账单 */
    public static ReconciliationEvent generate(String billNo, String bankCode, String sceneId, FsmOrder initialBill) {
        ReconciliationEvent e = new ReconciliationEvent("GENERATE", billNo);
        e.bizCode = bankCode;
        e.sceneId = sceneId;
        e.fsmOrder = initialBill;
        return e;
    }

    /** 用于状态流转事件，携带期望状态做乐观锁校验 */
    public static ReconciliationEvent of(String eventType, String billNo, String expectedState) {
        ReconciliationEvent e = new ReconciliationEvent(eventType, billNo);
        e.expectedState = expectedState;
        return e;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getOrderId() {
        return billNo == null ? UUID.randomUUID().toString() : billNo;
    }

    @Override
    public String orderState() {
        return expectedState;
    }

    @Override
    public boolean newCreate() {
        return fsmOrder != null;
    }

    @Override
    public String getBizCode() {
        return bizCode;
    }

    @Override
    public String getSceneId() {
        return sceneId;
    }

    @Override
    public FsmOrder buildFsmOrder() {
        return fsmOrder;
    }
}
