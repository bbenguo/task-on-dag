package com.mude.tod;

import com.mude.tod.task.TaskResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FlowResult {

    private String flowId;
    private String contextId;
    private Status status;
    private boolean success;
    private List<TaskResult<?>> taskResultList;

    private long startedAt;
    private long completedAt;
    private long duration;


    public enum Status {
        SUCCESS,
        FAILURE,
        PARTIAL
    }
}
