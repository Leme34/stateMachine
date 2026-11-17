package com.lsd.stateMachine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修复：
 * 1. 原 isSuccess 默认 false，导致大量 new ServiceResult() 被当作失败；新增静态工厂 ok()/fail() 统一语义。
 * 2. 保留无参构造默认 false（显式才表示失败）。
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

    public static <T, C> ServiceResult<T, C> ok() {
        ServiceResult<T, C> r = new ServiceResult<>();
        r.setSuccess(true);
        r.setMsg("success");
        return r;
    }

    public static <T, C> ServiceResult<T, C> ok(T data, String msg) {
        ServiceResult<T, C> r = ok();
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    public static <T, C> ServiceResult<T, C> fail(String msg) {
        ServiceResult<T, C> r = new ServiceResult<>();
        r.setSuccess(false);
        r.setMsg(msg);
        return r;
    }
}
