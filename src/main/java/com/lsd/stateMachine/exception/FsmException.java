package com.lsd.stateMachine.exception;

import com.lsd.stateMachine.enums.ErrorCodeEnum;

/**
 * Created by lsd
 * 2021-05-11 11:34
 */
public class FsmException extends RuntimeException {
    public FsmException(ErrorCodeEnum errCode) {
    }
}
