package com.crionuke.bolts.example.udpechoserver;

import com.crionuke.bolts.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
final class ThreadPool {
    static private final Logger logger = LoggerFactory.getLogger(ThreadPool.class);
    static private final int THREAD_POOL_SIZE = 32;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    ThreadPool() {
        threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(THREAD_POOL_SIZE);
        threadPoolTaskExecutor.initialize();
        logger.info("Thread pool with size={} created", THREAD_POOL_SIZE);
    }

    void execute(Worker service) {
        threadPoolTaskExecutor.execute(service);
    }
}
