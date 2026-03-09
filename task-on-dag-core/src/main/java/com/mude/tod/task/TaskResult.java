package com.mude.tod.task;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskResult<R> {

    public TaskResult(String taskId){
        this.taskId = taskId;
    }

    private R result;
    private String taskId;

    private long submitAt;
    private long startedAt;
    private long completedAt;
    private long duration;

    private TaskStatus status = TaskStatus.READY;
    private boolean success;
    private String message;
    private Throwable exception;

    private String threadName = Thread.currentThread().getName();
}
