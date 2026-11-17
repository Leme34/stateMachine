package com.lsd.reconciliation.plugin;

import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.annotation.ProcessorPlugin;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.reconciliation.context.ReconciliationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 审计日志插件：比对完成后在 action 与 save 之间写入审计追踪号。
 * 同时标注 @ProcessorPlugin，可被 DefaultPluginRegistry 装载（验证插件注册修复）。
 */
@ProcessorPlugin(state = "COMPARING", event = "FINISH_COMPARE", bizCode = "ICBC", sceneId = "DAILY")
public class AuditLogPluginHandler implements PluginHandler<String, ReconciliationContext> {

    /** 插件执行次数，用于测试实证插件机制已修复（P1 BUG 2） */
    private static final AtomicInteger executedCount = new AtomicInteger(0);

    public static int getExecutedCount() {
        return executedCount.get();
    }

    public static void resetExecutedCount() {
        executedCount.set(0);
    }

    @Override
    public ServiceResult action(StateContext<ReconciliationContext> context) throws Exception {
        ReconciliationContext c = context.getContext();
        if (c != null) {
            c.setAuditTraceId("AUDIT-" + UUID.randomUUID().toString().substring(0, 8));
        }
        executedCount.incrementAndGet();
        return ServiceResult.ok();
    }
}
