package com.lsd.reconciliation.enums;

/** 银企对账单状态迁移事件 */
public enum ReconciliationEventEnum {
    GENERATE("生成对账单"),
    START_COMPARE("开始比对"),
    FINISH_COMPARE("比对完成"),
    CONFIRM("确认"),
    CLOSE("关闭"),
    ;

    private final String desc;

    ReconciliationEventEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
