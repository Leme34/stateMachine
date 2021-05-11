package com.lsd.stateMachine.engine;

import com.lsd.stateMachine.annotation.processor.OrderProcessor;
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
 * （1）状态机引擎初始化阶段：构造状态处理器容器
 * <p>
 * 首先在代码编写阶段、根据上面的分析，业务通过实现AbstractStateProcessor模板类、并添加@OrderProcessor注解来实现自己的多个需要的特定状态处理器。
 * 那么在系统初始化阶段，所有添加了@OrderProcessor注解的实现类都会被spring所管理成为spring bean，
 * 状态机引擎在通过监听spring bean的注册（BeanPostProcessor）来将这些状态处理器processor装载到自己管理的容器中。
 * <p>
 * 直白来说、这个状态处理器容器其实就是一个多层map实现的，
 * 第一层map的key是状态（state），
 * 第二层map的key是状态对应的事件（event）、一个状态可以有多个要处理的事件，
 * 第三层map的key是具体的场景code（也就是bizCode和sceneId的组合），
 * 最后的value是AbstractStateProcessor集合。
 */
@Component
public class DefaultStateProcessRegistry implements BeanPostProcessor {
    /**
     * 第一层key是订单状态。
     * 第二层key是订单状态对应的事件，一个状态可以有多个事件。
     * 第三层key是具体场景code，场景下对应的多个处理器，需要后续进行过滤选择出一个具体的执行。
     */
    private static final Map<String, Map<String, Map<String, List<AbstractStateProcessor>>>> stateProcessMap = new ConcurrentHashMap<>();

    /**
     * 获取bean注解，对状态机引擎的处理器注解 {@link OrderProcessor} 进行解析处理
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractStateProcessor && bean.getClass().isAnnotationPresent(OrderProcessor.class)) {
            OrderProcessor annotation = bean.getClass().getAnnotation(OrderProcessor.class);
            String[] states = Arrays.stream(annotation.state()).map(Enum::name).toArray(String[]::new); //状态
            String event = annotation.event().name(); //事件
            String[] bizCodes = annotation.bizCode().length == 0 ? new String[]{"#"} : annotation.bizCode(); //业务编号
            String[] sceneIds = annotation.sceneId().length == 0 ? new String[]{"#"} : annotation.sceneId(); //场景编号
            initProcessMap(states, event, bizCodes, sceneIds, stateProcessMap, (AbstractStateProcessor) bean);
        }
        return bean;
    }

    /**
     * 将这些具体的场景的 状态、事件、对应的处理器 processor 装载到状态处理器容器 stateProcessMap 中
     *
     * @param states    状态
     * @param event     事件
     * @param bizCodes  业务编号
     * @param sceneIds  场景编号
     * @param map       状态处理器容器
     * @param processor 状态处理器
     */
    private <E extends StateProcessor> void initProcessMap(String[] states, String event, String[] bizCodes, String[] sceneIds,
                                                           Map<String, Map<String, Map<String, List<E>>>> map, E processor) {
        // 具体的场景code = bizCode + "@" + sceneId（也就是bizCode和sceneId的组合）
        for (String bizCode : bizCodes) {
            for (String sceneId : sceneIds) {
                Arrays.asList(states).parallelStream().forEach(state ->
                        registerStateHandlers(state, event, bizCode, sceneId, map, processor)
                );
            }
        }
    }

    /**
     * 初始化状态机处理器
     */
    public <E extends StateProcessor> void registerStateHandlers(String orderState, String event, String bizCode, String sceneId,
                                                                 Map<String, Map<String, Map<String, List<E>>>> map, E processor) {
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
        Map<String, List<E>> processorMap = stateTransformEventEnumMap.get(event);
        String bizCodeAndSceneId = bizCode + "@" + sceneId;
        if (!processorMap.containsKey(bizCodeAndSceneId)) {
            processorMap.put(bizCodeAndSceneId, new CopyOnWriteArrayList<>());
        }
        // 最后的value是AbstractStateProcessor集合
        processorMap.get(bizCodeAndSceneId).add(processor);
    }

    /**
     * 根据 state+event+bizCode+sceneId 获取所对应的业务处理器集合
     *
     * @param state   状态
     * @param event   事件
     * @param bizCode 业务编号
     * @param sceneId 场景编号
     * @return 处理器集合
     */
    public List<AbstractStateProcessor> acquireStateProcess(String state, String event, String bizCode, String sceneId) {
        Map<String, Map<String, List<AbstractStateProcessor>>> stateTransformEventEnumMap = stateProcessMap.get(state);
        Map<String, List<AbstractStateProcessor>> processorMap = stateTransformEventEnumMap.get(event);
        String bizCodeAndSceneId = bizCode + "@" + sceneId;
        return processorMap.getOrDefault(bizCodeAndSceneId, Collections.emptyList());
    }

}
