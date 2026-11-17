package com.lsd.stateMachine.plugin;

import com.lsd.stateMachine.plugin.annotation.ProcessorPlugin;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.processor.StateProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * （1）状态机插件初始化阶段
 * <p>
 * 修复点：
 * 1. 注册条件由 {@code bean instanceof AbstractStateProcessor} 改为 {@code bean instanceof PluginHandler}，
 *    否则插件永远无法被装载（原 P1 缺陷）；
 * 2. state/event 改 String；
 * 3. acquire 空值保护 + 通配符回退。
 */
@Component
public class DefaultPluginRegistry implements BeanPostProcessor {

    private static final Map<String, Map<String, Map<String, List<PluginHandler>>>> statePluginMap =
            new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PluginHandler && bean.getClass().isAnnotationPresent(ProcessorPlugin.class)) {
            ProcessorPlugin annotation = bean.getClass().getAnnotation(ProcessorPlugin.class);
            String[] states = annotation.state();
            String event = annotation.event();
            String[] bizCodes = annotation.bizCode().length == 0 ? new String[]{"#"} : annotation.bizCode();
            String[] sceneIds = annotation.sceneId().length == 0 ? new String[]{"#"} : annotation.sceneId();
            initPluginMap(states, event, bizCodes, sceneIds, statePluginMap, (PluginHandler) bean);
        }
        return bean;
    }

    private <E extends StateProcessor> void initPluginMap(String[] states, String event, String[] bizCodes, String[] sceneIds,
                                                          Map<String, Map<String, Map<String, List<E>>>> map, E processor) {
        for (String bizCode : bizCodes) {
            for (String sceneId : sceneIds) {
                Arrays.asList(states).parallelStream().forEach(state ->
                        registerPluginHandlers(state, event, bizCode, sceneId, map, processor)
                );
            }
        }
    }

    public <E extends StateProcessor> void registerPluginHandlers(String orderState, String event, String bizCode, String sceneId,
                                                                  Map<String, Map<String, Map<String, List<E>>>> map, E pluginHandler) {
        map.computeIfAbsent(orderState, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(event, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(bizCode + "@" + sceneId, k -> new CopyOnWriteArrayList<>())
                .add(pluginHandler);
    }

    public List<PluginHandler> acquirePluginHandler(String state, String event, String bizCode, String sceneId) {
        Map<String, Map<String, List<PluginHandler>>> eventMap = statePluginMap.get(state);
        if (eventMap == null) {
            return Collections.emptyList();
        }
        Map<String, List<PluginHandler>> pluginMap = eventMap.get(event);
        if (pluginMap == null) {
            return Collections.emptyList();
        }
        String b = bizCode == null ? "#" : bizCode;
        String s = sceneId == null ? "#" : sceneId;
        List<PluginHandler> exact = pluginMap.get(b + "@" + s);
        if (exact != null && !exact.isEmpty()) {
            return exact;
        }
        List<PluginHandler> result = new ArrayList<>();
        addIfPresent(result, pluginMap, b + "@#");
        addIfPresent(result, pluginMap, "#@" + s);
        addIfPresent(result, pluginMap, "#@#");
        return result;
    }

    private void addIfPresent(List<PluginHandler> result, Map<String, List<PluginHandler>> map, String key) {
        List<PluginHandler> list = map.get(key);
        if (list != null) {
            result.addAll(list);
        }
    }

    public static Map<String, Map<String, Map<String, List<PluginHandler>>>> getStatePluginMap() {
        return statePluginMap;
    }
}
