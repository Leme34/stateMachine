package com.lsd.stateMachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 修复：显式扫描 com.lsd 根包，使 reconciliation 等业务子模块的处理器/插件 bean 被容器纳管。
 */
@SpringBootApplication(scanBasePackages = "com.lsd")
public class StateMachineApplication {

    public static void main(String[] args) {
        SpringApplication.run(StateMachineApplication.class, args);
    }
}
