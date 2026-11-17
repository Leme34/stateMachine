package com.lsd.stateMachine.processor;

import com.lsd.stateMachine.checker.Checkable;
import com.lsd.stateMachine.checker.CheckerExecutor;
import com.lsd.stateMachine.context.StateContext;
import com.lsd.stateMachine.plugin.PluginHandlerExecutor;
import com.lsd.stateMachine.plugin.PluginHandlerable;
import com.lsd.stateMachine.vo.ServiceResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 状态机处理器模板类
 * <p>
 * 修复点：
 * 1. getNextState 返回 String，不再调用 .name()；
 * 2. 恢复并行校验（asyncChecker）与插件执行（在 action 与 save 之间）；
 * 3. 在 finally 中释放需要 release 的 checker / plugin。
 */
@Component
public abstract class AbstractStateProcessor<T, C> implements StateProcessor<T, C>, StateActionStep<T, C> {

    @Resource
    private CheckerExecutor<T, C> checkerExecutor;
    @Resource
    private PluginHandlerExecutor<T, C> pluginHandlerExecutor;

    /** 多处理器时的二次筛选钩子 */
    public abstract boolean filter(StateContext<C> context);

    @Override
    public final ServiceResult<T, C> action(StateContext<C> context) throws Exception {
        Checkable checkable = this.getCheckable(context);
        PluginHandlerable pluginHandlerable = this.getPluginHandlerable(context);
        ServiceResult<T, C> result = null;
        try {
            // 1) 参数校验（串行）
            result = checkerExecutor.serialCheck(checkable.getParamChecker(), context);
            if (!result.isSuccess()) {
                return result;
            }
            // 2) 数据准备
            this.prepare(context);
            // 3) 同步校验（串行）
            result = checkerExecutor.serialCheck(checkable.getSyncChecker(), context);
            if (!result.isSuccess()) {
                return result;
            }
            // 4) 异步校验（并行）—— 已恢复
            result = checkerExecutor.parallelCheck(checkable.getAsyncChecker(), context);
            if (!result.isSuccess()) {
                return result;
            }
            // 5) 下一状态判定（须在 prepare 之后）
            String nextState = this.getNextState(context);
            // 6) 业务逻辑
            result = this.action(nextState, context);
            if (!result.isSuccess()) {
                return result;
            }
            // 7) 插件逻辑（action 与 save 之间）—— 已恢复
            result = this.pluginHandlerExecutor.parallelExecutor(pluginHandlerable.getAsyncPluginHandler(), context);
            if (!result.isSuccess()) {
                return result;
            }
            // 8) 持久化
            result = this.save(nextState, context);
            if (!result.isSuccess()) {
                return result;
            }
            // 9) 后续处理
            this.after(context);
            return result;
        } finally {
            // 修复：释放 needRelease=true 的 checker / plugin
            if (checkable != null) {
                checkerExecutor.releaseCheck(checkable, context, result);
            }
            if (pluginHandlerable != null) {
                pluginHandlerExecutor.releaseExecutor(pluginHandlerable, context, result);
            }
        }
    }
}
