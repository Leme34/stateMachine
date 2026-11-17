package com.lsd.stateMachine.plugin;

import com.lsd.stateMachine.plugin.handler.PluginHandler;

import java.util.Collections;
import java.util.List;

/**
 * 状态机插件执行器
 */
public interface PluginHandlerable {
    /**
     * 需同步执行的状态检查器
     */
    default List<PluginHandler> getSyncPluginHandler() {
        return Collections.EMPTY_LIST;
    }

    /**
     * 可异步执行的校验器
     */
    default List<PluginHandler> getAsyncPluginHandler() {
        return Collections.EMPTY_LIST;
    }
}
