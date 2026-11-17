package com.lsd.reconciliation.checker;

import com.lsd.stateMachine.checker.Checker;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.vo.ServiceResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/** 异步校验器（并行执行）：风险规则校验，演示 needRelease 释放语义 */
@Component
public class RiskChecker implements Checker {

    /** 释放次数计数器，用于测试实证 release 逻辑未被反转（P1 BUG 3） */
    private static final AtomicInteger releaseCount = new AtomicInteger(0);

    public static int getReleaseCount() {
        return releaseCount.get();
    }

    public static void resetReleaseCount() {
        releaseCount.set(0);
    }

    @Override
    public ServiceResult check(StateContext context) {
        // 模拟风控校验通过
        return ServiceResult.ok();
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public boolean needRelease() {
        return true; // 需要释放
    }

    @Override
    public void release(StateContext context, ServiceResult result) {
        // 模拟释放风控上下文/锁
        releaseCount.incrementAndGet();
    }
}
