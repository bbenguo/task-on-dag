package com.mude.tod.base;

public interface Context {

    String getId();

    Object get(String taskId);

    void put(String taskId, Object value);
}
