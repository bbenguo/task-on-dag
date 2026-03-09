package com.mude.tod;

import com.mude.tod.base.Context;
import com.mude.tod.task.Task;

public class TestTask extends TestNode implements Task<String> {

    public TestTask(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public void beforeExecute(Context context) {

    }

    @Override
    public String execute(Context context) {
        return "R:" + getId();
    }

    @Override
    public void afterExecute(Context context, String result) {

    }

    @Override
    public void onError(Context context, Throwable e) {

    }
}
