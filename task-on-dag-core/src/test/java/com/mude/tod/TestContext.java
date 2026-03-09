package com.mude.tod;

import com.mude.tod.base.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestContext implements Context {

    private final Map<String, Object> delegate = new ConcurrentHashMap<>();

    @Override
    public String getId() {
        return "";
    }

    @Override
    public Object get(String taskId) {
        return delegate.get(taskId);
    }

    @Override
    public void put(String taskId, Object value) {
        delegate.put(taskId, value);
    }
}
