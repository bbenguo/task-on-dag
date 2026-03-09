package com.mude.tod;

import com.mude.tod.dag.IVertex;
import lombok.ToString;

@ToString
public class TestNode implements IVertex {

    private final String id;

    public TestNode(String id) {
        this.id = id;
    }
    @Override
    public String getId() {
        return id;
    }
}
