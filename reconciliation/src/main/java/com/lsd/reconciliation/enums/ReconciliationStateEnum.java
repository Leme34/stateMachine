package com.lsd.reconciliation.enums;

/** 银企对账单状态 */
public enum ReconciliationStateEnum {
    INIT("待生成"),
    GENERATED("已生成待比对"),
    COMPARING("比对中"),
    DIFFERENT("存在差异"),
    CONFIRMED("已确认平账"),
    CLOSED("已关闭"),
    ;

    private final String desc;

    ReconciliationStateEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
