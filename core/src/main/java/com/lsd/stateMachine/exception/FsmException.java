package com.lsd.stateMachine.exception;

import com.lsd.stateMachine.enums.ErrorCodeEnum;

/**
 * 修复：原构造函数为空实现，丢弃了错误码与错误信息；现保存错误码并调用 super(message)。
 */
public class FsmException extends RuntimeException {

    private final ErrorCodeEnum errCode;

    public FsmException(ErrorCodeEnum errCode) {
        super(errCode == null ? "FSM error" : errCode.getErrMsg());
        this.errCode = errCode;
    }

    public ErrorCodeEnum getErrCode() {
        return errCode;
    }
}
