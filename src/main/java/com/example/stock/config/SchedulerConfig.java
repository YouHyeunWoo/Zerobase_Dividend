package com.example.stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //ThreadPool 여러 개의 스레드를 유지/관리
        //ThreadPool의 적정 사이즈 = 1.CPU 처리가 많은 경우 >> CPU의 코어 개수 n개 + 1로 설정
        //I/O작업이 많은 경우 >> CPU의 코어 개수 n개 * 2 로 일반적으로 설정
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        int n = Runtime.getRuntime().availableProcessors(); //CPU의 코어 개수
        threadPool.setPoolSize(n + 1); //또는 n * 2
        threadPool.initialize();

        taskRegistrar.setTaskScheduler(threadPool);
    }
}
