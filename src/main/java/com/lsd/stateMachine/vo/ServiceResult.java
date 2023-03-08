package com.lsd.stateMachine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lsd
 * 2021-05-10 17:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResult<T, C> {

    private T data;
    private C context;
    private String msg;
    private boolean isSuccess = false;

    public boolean isSuccess() {
        return isSuccess;
    }

}
