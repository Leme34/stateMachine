package com.lsd.stateMachine.checker;

import com.lsd.stateMachine.vo.ServiceResult;
import com.lsd.stateMachine.context.StateContext;
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
 * 校验器的执行器
 */
@Slf4j
@Component
public class CheckerExecutor<T, C> {

    // 线程池应该根据业务情况而定
    private final static ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 执行串行校验器
     */
    public ServiceResult<T, C> serialCheck(List<Checker> paramCheckers, StateContext<C> context) {
        if (!CollectionUtils.isEmpty(paramCheckers)) {
            if (paramCheckers.size() == 1) {
                return paramCheckers.get(0).check(context);
            }
            paramCheckers.sort(Comparator.comparingInt(Checker::order));
            for (Checker c : paramCheckers) {
                ServiceResult result = c.check(context);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }
        ServiceResult result = new ServiceResult();
        result.setContext(context);
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    /**
     * 执行并行校验器，
     * 按照任务投递的顺序判断返回。
     */
    public ServiceResult<T, C> parallelCheck(List<Checker> checkers, StateContext<C> context) {
        if (!CollectionUtils.isEmpty(checkers)) {
            if (checkers.size() == 1) {
                return checkers.get(0).check(context);
            }
            List<Future<ServiceResult>> resultList = Collections.synchronizedList(new ArrayList<>(checkers.size()));
            checkers.sort(Comparator.comparingInt(Checker::order));
            for (Checker c : checkers) {
                Future<ServiceResult> future = executor.submit(() -> c.check(context));
                resultList.add(future);
            }
            for (Future<ServiceResult> future : resultList) {
                try {
                    ServiceResult sr = future.get();
                    if (!sr.isSuccess()) {
                        return sr;
                    }
                } catch (Exception e) {
                    log.error("parallelCheck executor.submit error.", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return new ServiceResult<>();
    }

    /**
     * 执行checker的释放操作
     */
    public <T, C> void releaseCheck(Checkable checkable, StateContext<C> context, ServiceResult<T, C> result) {
        List<Checker> checkers = new ArrayList<>();
        checkers.addAll(checkable.getParamChecker());
        checkers.addAll(checkable.getSyncChecker());
        checkers.addAll(checkable.getAsyncChecker());
        checkers.removeIf(Checker::needRelease);
        if (!CollectionUtils.isEmpty(checkers)) {
            if (checkers.size() == 1) {
                checkers.get(0).release(context, result);
                return;
            }
            CountDownLatch latch = new CountDownLatch(checkers.size());
            for (Checker c : checkers) {
                executor.execute(() -> {
                    try {
                        c.release(context, result);
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
