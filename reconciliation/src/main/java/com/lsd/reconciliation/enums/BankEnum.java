package com.lsd.reconciliation.enums;

/** 对账业务维度：银行渠道（作为 bizCode） */
public enum BankEnum {
    ICBC("工商银行"),
    CCB("建设银行"),
    BOC("中国银行"),
    ;

    private final String desc;

    BankEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
