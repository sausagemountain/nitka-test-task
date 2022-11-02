package com.sausagemountain.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiTaskQueue<K> {
    private final int threadCount;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<K, ConcurrentLinkedQueue<Runnable>> tasksQueue;
    private final ConcurrentHashMap<K, Future<?>> tasks;

    private Thread runner = null;
    private boolean finish = false;
    private Runnable endTask = null;

    public MultiTaskQueue(int threadCount) {
        this.threadCount = threadCount;
        this.executorService = Executors.newFixedThreadPool(this.threadCount);
        this.tasksQueue = new ConcurrentHashMap<>();
        this.tasks = new ConcurrentHashMap<>();
    }

    public void add(K key, Runnable value) {
        tasksQueue.computeIfAbsent(key, e -> new ConcurrentLinkedQueue<>()).add(value);
    }

    private void runTasksInternal() {
        boolean allEmpty = false;
        finish = false;
        while (!allEmpty) {
            final List<K> empty = new ArrayList<>();
            for (var e : tasksQueue.entrySet()) {
                final K key = e.getKey();
                final ConcurrentLinkedQueue<Runnable> queue = e.getValue();

                if (!queue.isEmpty()) {
                    if (!tasks.containsKey(key) || tasks.get(key).isDone()) {
                        final Runnable run = queue.poll();
                        if (run != null) {
                            final Future<?> future = executorService.submit(run);
                            tasks.put(key, future);
                        } else {
                            empty.add(key);
                        }
                    }
                } else {
                    empty.add(key);
                }
                if (tasks.containsKey(key) && tasks.get(key).isDone()) {
                    tasks.remove(key);
                }
            }
            if (tasksQueue.size() == empty.size() && tasks.isEmpty() && finish) {
                allEmpty = true;
            }
        }
        if (endTask != null) {
            endTask.run();
        }
    }

    public void runTasks() {
        runner = new Thread(this::runTasksInternal);
        runner.start();
    }

    public void addEndTask(Runnable run) {
        endTask = run;
    }

    public void stop() {
        if (!this.finish) {
            this.finish = true;
        } else {
            runner.interrupt();
        }
    }

}
