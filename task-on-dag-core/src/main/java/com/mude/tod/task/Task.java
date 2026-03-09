package com.mude.tod.task;

import com.mude.tod.base.Context;
import com.mude.tod.dag.IVertex;

import java.time.Duration;

public interface Task<R> extends IVertex {

    String getName();

    void beforeExecute(Context context);

    R execute(Context context);

    void afterExecute(Context context, R result);

    void onError(Context context, Throwable e);

    default Duration expiredIn(Context context) {
        return Duration.ofSeconds(30L);
    }
}
