package com.lsd.stateMachine.plugin;

import com.lsd.stateMachine.checker.CheckerExecutor;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.vo.ServiceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 插件的执行器
 * 参考：{@link CheckerExecutor}
 */
@Slf4j
@Component
public class PluginHandlerExecutor<T, C> {

    // 线程池应该根据业务情况而定
    private final static ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 执行串行校验器
     */
    public ServiceResult<T, C> serialExecutor(List<PluginHandler> pluginHandlers, StateContext<C> context) {
        if (!CollectionUtils.isEmpty(pluginHandlers)) {
            if (pluginHandlers.size() == 1) {
                try {
                    return pluginHandlers.get(0).action(context);
                } catch (Exception e) {
                    log.error("serialExecutor error.", e);
                    throw new RuntimeException(e);
                }
            }
            pluginHandlers.sort(Comparator.comparingInt(PluginHandler::order));
            for (PluginHandler c : pluginHandlers) {
                ServiceResult result;
                try {
                    result = c.action(context);
                } catch (Exception e) {
                    log.error("serialExecutor error.", e);
                    throw new RuntimeException(e);
                }
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }
        return new ServiceResult<>();
    }

    /**
     * 执行并行校验器，
     * 按照任务投递的顺序判断返回。
     */
    public ServiceResult<T, C> parallelExecutor(List<PluginHandler> pluginHandlers, StateContext<C> context) {
        if (!CollectionUtils.isEmpty(pluginHandlers)) {
            if (pluginHandlers.size() == 1) {
                try {
                    return pluginHandlers.get(0).action(context);
                } catch (Exception e) {
                    log.error("parallelExecutor executor.submit error.", e);
                    throw new RuntimeException(e);
                }
            }
            List<Future<ServiceResult>> resultList = Collections.synchronizedList(new ArrayList<>(pluginHandlers.size()));
            pluginHandlers.sort(Comparator.comparingInt(PluginHandler::order));
            for (PluginHandler pluginHandler : pluginHandlers) {
                Future<ServiceResult> future = executor.submit(() -> pluginHandler.action(context));
                resultList.add(future);
            }
            for (Future<ServiceResult> future : resultList) {
                try {
                    ServiceResult sr = future.get();
                    if (!sr.isSuccess()) {
                        return sr;
                    }
                } catch (Exception e) {
                    log.error("parallelExecutor executor.submit error.", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return new ServiceResult<>();
    }

    /**
     * 执行pluginHandler的释放操作
     */
    public <T, C> void releaseExecutor(PluginHandlerable pluginHandlerable, StateContext<C> context, ServiceResult<T, C> result) {
        List<PluginHandler> pluginHandlers = new ArrayList<>();
        pluginHandlers.addAll(pluginHandlerable.getSyncPluginHandler());
        pluginHandlers.addAll(pluginHandlerable.getAsyncPluginHandler());
        pluginHandlers.removeIf(PluginHandler::needRelease);
        if (!CollectionUtils.isEmpty(pluginHandlers)) {
            if (pluginHandlers.size() == 1) {
                pluginHandlers.get(0).release(context, result);
                return;
            }
            CountDownLatch latch = new CountDownLatch(pluginHandlers.size());
            for (PluginHandler handler : pluginHandlers) {
                executor.execute(() -> {
                    try {
                        handler.release(context, result);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
