package com.lsd.stateMachine.plugin;

import com.lsd.stateMachine.plugin.annotation.ProcessorPlugin;
import com.lsd.stateMachine.plugin.handler.PluginHandler;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
import com.lsd.stateMachine.processor.StateProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * （1）状态机插件初始化阶段
 * 参考 {@link com.lsd.stateMachine.engine.DefaultStateProcessRegistry}
 */
@Component
public class DefaultPluginRegistry implements BeanPostProcessor {
    /**
     * 第一层key是订单状态。
     * 第二层key是订单状态对应的事件，一个状态可以有多个事件。
     * 第三层key是具体场景code，场景下对应的多个处理器，需要后续进行过滤选择出一个具体的执行。
     */
    private static final Map<String, Map<String, Map<String, List<PluginHandler>>>> statePluginMap = new ConcurrentHashMap<>();

    /**
     * 获取bean注解，对插件注解 {@link ProcessorPlugin} 进行解析处理
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractStateProcessor && bean.getClass().isAnnotationPresent(ProcessorPlugin.class)) {
            ProcessorPlugin annotation = bean.getClass().getAnnotation(ProcessorPlugin.class);
            String[] states = Arrays.stream(annotation.state()).map(Enum::name).toArray(String[]::new); //状态
            String event = annotation.event().name(); //事件
            String[] bizCodes = annotation.bizCode().length == 0 ? new String[]{"#"} : annotation.bizCode(); //业务编号
            String[] sceneIds = annotation.sceneId().length == 0 ? new String[]{"#"} : annotation.sceneId(); //场景编号
            initPluginMap(states, event, bizCodes, sceneIds, statePluginMap, (PluginHandler) bean);
        }
        return bean;
    }

    private <E extends StateProcessor> void initPluginMap(String[] states, String event, String[] bizCodes, String[] sceneIds,
                                                          Map<String, Map<String, Map<String, List<E>>>> map, E processor) {
        // 具体的场景code = bizCode + "@" + sceneId（也就是bizCode和sceneId的组合）
        for (String bizCode : bizCodes) {
            for (String sceneId : sceneIds) {
                Arrays.asList(states).parallelStream().forEach(state ->
                        registerPluginHandlers(state, event, bizCode, sceneId, map, processor)
                );
            }
        }
    }

    /**
     * 初始化状态机处理器
     */
    public <E extends StateProcessor> void registerPluginHandlers(String orderState, String event, String bizCode, String sceneId,
                                                                  Map<String, Map<String, Map<String, List<E>>>> map, E pluginHandler) {
        // 第一层key，state维度
        if (!map.containsKey(orderState)) {
            map.put(orderState, new ConcurrentHashMap<>());
        }
        // 第二层key，event维度
        Map<String, Map<String, List<E>>> stateTransformEventEnumMap = map.get(orderState);
        if (!stateTransformEventEnumMap.containsKey(event)) {
            stateTransformEventEnumMap.put(event, new ConcurrentHashMap<>());
        }
        // 第三层key，bizCode and sceneId
        Map<String, List<E>> pluginHandlerMap = stateTransformEventEnumMap.get(event);
        String bizCodeAndSceneId = bizCode + "@" + sceneId;
        if (!pluginHandlerMap.containsKey(bizCodeAndSceneId)) {
            pluginHandlerMap.put(bizCodeAndSceneId, new CopyOnWriteArrayList<>());
        }
        // 最后的value是AbstractStateProcessor集合
        pluginHandlerMap.get(bizCodeAndSceneId).add(pluginHandler);
    }

    public List<PluginHandler> acquirePluginHandler(String state, String event, String bizCode, String sceneId) {
        Map<String, Map<String, List<PluginHandler>>> stateTransformEventEnumMap = statePluginMap.get(state);
        Map<String, List<PluginHandler>> pluginHandlerMap = stateTransformEventEnumMap.get(event);
        String bizCodeAndSceneId = bizCode + "@" + sceneId;
        return pluginHandlerMap.getOrDefault(bizCodeAndSceneId, Collections.emptyList());
    }

    public static Map<String, Map<String, Map<String, List<PluginHandler>>>> getStatePluginMap() {
        return statePluginMap;
    }
}
