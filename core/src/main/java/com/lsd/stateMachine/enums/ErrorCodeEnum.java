package com.lsd.stateMachine.enums;

/**
 * 修复：原构造函数丢弃了 errMsg，现保存并提供 getter。
 */
public enum ErrorCodeEnum {
    ORDER_NOT_FOUND("订单不存在"),
    ORDER_STATE_NOT_MATCH("订单状态不匹配"),
    NOT_FOUND_PROCESSOR("未找到处理器"),
    FOUND_MORE_PROCESSOR("处理器数量超过上限"),
    ;

    private final String errMsg;

    ErrorCodeEnum(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
