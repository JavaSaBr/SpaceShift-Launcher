package com.ss.launcher;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Менеджер по фоновым процессам.
 *
 * @author Ronn
 */
public class ExecutorManager {

    private static final ExecutorManager INSTANCE = new ExecutorManager();

    public static ExecutorManager getInstance() {
        return INSTANCE;
    }

    /**
     * Асинхронный исполнитель задач.
     */
    private final Executor asynExecutor;

    private ExecutorManager() {
        this.asynExecutor = Executors.newFixedThreadPool(2);
    }

    public void async(Runnable runnable) {
        asynExecutor.execute(runnable);
    }
}
