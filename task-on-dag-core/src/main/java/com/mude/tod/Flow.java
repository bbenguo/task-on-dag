package com.mude.tod;

import com.mude.tod.base.Context;
import com.mude.tod.dag.Graph;
import com.mude.tod.task.Task;
import com.mude.tod.task.TaskResult;
import com.mude.tod.task.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Flow {

    @Getter
    private final String id;
    private final Graph<Task<?>> graph = new Graph<>();

    public Flow(String id) {
        this.id = id;
    }

    public void addTask(Task<?> task, Task<?>... dependOn) {
        if (dependOn.length == 0) {
            graph.addEdge(task, null);
            return;
        }

        for(Task<?> t : dependOn){
            graph.addEdge(task, t);
        }
    }

    public boolean validate() {
        return graph.isAcyclic();
    }

    /**
     * 单线程执行任务
     * @param context
     * @param direction
     * @return
     */
    public FlowResult execute(final Context context, final TaskDirection direction) {
        long startedAt = System.currentTimeMillis();
        List<List<Task<?>>> taskList = graph.topologicalSort(direction == TaskDirection.ROOT_FIRST);
        List<TaskResult<?>> taskResultList = new ArrayList<>();
        for (List<Task<?>> list : taskList) {
            boolean hasError = false;
            for (Task<?> task : list) {
                long submittedAt = System.currentTimeMillis();
                TaskResult<?> result = applyTask(task, context);
                result.setSubmitAt(submittedAt);
                taskResultList.add(result);
                if (!result.isSuccess()) {
                    hasError = true;
                    break;
                }
            }
            if (hasError) {
                break;
            }
        }
        FlowResult result = makeFlowResult(taskResultList, taskList);
        result.setStartedAt(startedAt);
        result.setCompletedAt(System.currentTimeMillis());
        result.setDuration(result.getCompletedAt() - startedAt);
        result.setContextId(context.getId());
        return result;
    }

    /**
     * 异步执行任务
     * @param context
     * @param executor
     * @param direction
     * @param callback
     */
    public void asyncExecute(final Context context, final ExecutorService executor, final TaskDirection direction, final Callback callback) {
        executor.submit(() -> applyAsyncExecute(context, executor, direction, callback));
    }

    private void applyAsyncExecute(final Context context, final ExecutorService executor, final TaskDirection direction, final Callback callback) {
        long startedAt = System.currentTimeMillis();
        List<List<Task<?>>> taskList = graph.topologicalSort(direction == TaskDirection.ROOT_FIRST);
        List<TaskResult<?>> taskResultList = new ArrayList<>();

        for (List<Task<?>> list : taskList) {
            boolean hasError = false;
            List<AsyncFuture> futures = new ArrayList<>();
            for (Task<?> task : list) {
                Future<TaskResult<?>> submit = executor.submit(() -> applyTask(task, context));
                AsyncFuture future = new AsyncFuture();
                future.setFuture(submit);
                future.setSubmitAt(System.currentTimeMillis());
                future.setTask(task);
                futures.add(future);
            }
            for (AsyncFuture future : futures) {
                TaskResult<?> result = new TaskResult<>(future.getTask().getId());
                try {
                    result = future.getFuture().get(future.getTask().expiredIn(context).getSeconds(), TimeUnit.SECONDS);
                    result.setSubmitAt(future.getSubmitAt());
                } catch (Throwable e) {
                    result.setSuccess(false);
                    result.setException(e);
                    result.setMessage(e.getMessage());
                    result.setStatus(TaskStatus.CANCELED);
                    future.getFuture().cancel(true);

                    future.getTask().onError(context, e);
                }
                finally {
                    taskResultList.add(result);
                }
                if (!result.isSuccess() && !hasError) {
                    hasError = true;
                }
            }

            if (hasError) {
                break;
            }
        }

        FlowResult result = makeFlowResult(taskResultList, taskList);
        result.setStartedAt(startedAt);
        result.setCompletedAt(System.currentTimeMillis());
        result.setDuration(result.getCompletedAt() - startedAt);
        result.setContextId(context.getId());
        callback.complete(context, result);
    }

    private <R> TaskResult<R> applyTask(Task<R> task, Context context) {
        TaskResult<R> result = new TaskResult<>(task.getId());
        try {
            result.setStartedAt(System.currentTimeMillis());
            result.setStatus(TaskStatus.RUNNING);

            task.beforeExecute(context);
            R executed = task.execute(context);
            context.put(task.getId(), executed);
            task.afterExecute(context, executed);
            result.setResult(executed);
            result.setSuccess(true);
            result.setMessage("SUCCESS");

        } catch (Throwable e) {
            result.setException(e);
            result.setMessage(e.getMessage());

            task.onError(context, e);
        }
        finally {
            result.setCompletedAt(System.currentTimeMillis());
            result.setStatus(TaskStatus.COMPLETED);
            result.setDuration(result.getCompletedAt() - result.getStartedAt());
        }

        return result;
    }

    private FlowResult makeFlowResult(List<TaskResult<?>> taskResultList,  List<List<Task<?>>> taskList) {
        FlowResult result = new FlowResult();
        result.setFlowId(getId());
        result.setTaskResultList(taskResultList);
        long successCount =  taskResultList.stream().filter(TaskResult::isSuccess).count();
        long taskCount = taskList.stream().mapToLong(List::size).sum();
        if (successCount == 0) {
            result.setSuccess(false);
            result.setStatus(FlowResult.Status.FAILURE);
        }
        else if (successCount < taskCount) {
            result.setSuccess(false);
            result.setStatus(FlowResult.Status.PARTIAL);
        }
        else {
            result.setSuccess(true);
            result.setStatus(FlowResult.Status.SUCCESS);
        }
        return result;
    }

    public interface Callback {
        void complete(Context context, FlowResult result);
    }

    @Setter
    @Getter
    public static class AsyncFuture  {
        private Future<TaskResult<?>> future;
        private Task<?> task;
        private long submitAt;
    }

    public enum TaskDirection {
        ROOT_FIRST, LEAF_FIRST
    }

}
