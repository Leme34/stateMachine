package com.lsd.stateMachine.engine;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
import com.lsd.stateMachine.processor.AbstractStateProcessor;
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
 * （1）状态机引擎初始化阶段：构造状态处理器容器
 * <p>
 * 三层 map：state -> event -> (bizCode@sceneId) -> List&lt;AbstractStateProcessor&gt;
 * <p>
 * 修复点：
 * 1. state/event 由枚举改为 String，支持任意业务域复用；
 * 2. acquireStateProcess 空值保护 + 通配符('#')回退匹配，非法 state/event 返回空集合而非 NPE。
 */
@Component
public class DefaultStateProcessRegistry implements BeanPostProcessor {

    private static final Map<String, Map<String, Map<String, List<AbstractStateProcessor>>>> stateProcessMap =
            new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractStateProcessor && bean.getClass().isAnnotationPresent(OrderProcessor.class)) {
            OrderProcessor annotation = bean.getClass().getAnnotation(OrderProcessor.class);
            String[] states = annotation.state();
            String event = annotation.event();
            String[] bizCodes = annotation.bizCode().length == 0 ? new String[]{"#"} : annotation.bizCode();
            String[] sceneIds = annotation.sceneId().length == 0 ? new String[]{"#"} : annotation.sceneId();
            initProcessMap(states, event, bizCodes, sceneIds, stateProcessMap, (AbstractStateProcessor) bean);
        }
        return bean;
    }

    private <E extends StateProcessor> void initProcessMap(String[] states, String event, String[] bizCodes, String[] sceneIds,
                                                           Map<String, Map<String, Map<String, List<E>>>> map, E processor) {
        for (String bizCode : bizCodes) {
            for (String sceneId : sceneIds) {
                Arrays.asList(states).parallelStream().forEach(state ->
                        registerStateHandlers(state, event, bizCode, sceneId, map, processor)
                );
            }
        }
    }

    public <E extends StateProcessor> void registerStateHandlers(String orderState, String event, String bizCode, String sceneId,
                                                                 Map<String, Map<String, Map<String, List<E>>>> map, E processor) {
        map.computeIfAbsent(orderState, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(event, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(bizCode + "@" + sceneId, k -> new CopyOnWriteArrayList<>())
                .add(processor);
    }

    /**
     * 根据 state+event+bizCode+sceneId 获取处理器集合（含通配符回退）。
     * 匹配优先级：biz@scene > biz@# > #@scene > #@#
     */
    public List<AbstractStateProcessor> acquireStateProcess(String state, String event, String bizCode, String sceneId) {
        Map<String, Map<String, List<AbstractStateProcessor>>> eventMap = stateProcessMap.get(state);
        if (eventMap == null) {
            return Collections.emptyList();
        }
        Map<String, List<AbstractStateProcessor>> processorMap = eventMap.get(event);
        if (processorMap == null) {
            return Collections.emptyList();
        }
        String b = bizCode == null ? "#" : bizCode;
        String s = sceneId == null ? "#" : sceneId;
        List<AbstractStateProcessor> result = new ArrayList<>();
        List<AbstractStateProcessor> exact = processorMap.get(b + "@" + s);
        if (exact != null && !exact.isEmpty()) {
            return exact;
        }
        addIfPresent(result, processorMap, b + "@#");
        addIfPresent(result, processorMap, "#@" + s);
        addIfPresent(result, processorMap, "#@#");
        return result;
    }

    private void addIfPresent(List<AbstractStateProcessor> result, Map<String, List<AbstractStateProcessor>> map, String key) {
        List<AbstractStateProcessor> list = map.get(key);
        if (list != null) {
            result.addAll(list);
        }
    }
}
