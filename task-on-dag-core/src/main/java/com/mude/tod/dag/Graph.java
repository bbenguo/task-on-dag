package com.mude.tod.dag;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 图
 */
public class Graph<N extends IVertex> {

    private boolean validateFlag = false;
    private final Map<String, VertexNode> nodeMap = new ConcurrentHashMap<>();
    private TopologicSortResult inDegreeFirstResult;
    private TopologicSortResult outDegreeFirstResult;

    public boolean isAcyclic() {
        if (!validateFlag) {
            validate();
            validateFlag = true;
        }
        return inDegreeFirstResult.isAcyclic;
    }

    public void addEdge(N source, N target) {
        if (source == null) {
            throw new IllegalArgumentException("source不能为空");
        }

        validateFlag = false;
        inDegreeFirstResult = null;
        outDegreeFirstResult = null;

        VertexNode sourceNode = nodeMap.get(source.getId());
        if (sourceNode == null) {
            sourceNode = new VertexNode(source);
            nodeMap.put(sourceNode.getId(), sourceNode);
        }

        if (target != null && source != target) {
            VertexNode targetNode = nodeMap.get(target.getId());
            if (targetNode == null) {
                targetNode = new VertexNode(target);
                nodeMap.put(targetNode.getId(), targetNode);
            }
            sourceNode.childMap.put(targetNode.getId(), targetNode);
            targetNode.parentMap.put(sourceNode.getId(), sourceNode);
        }
    }


    public List<List<N>> topologicalSort(boolean inDegreeFirst) {
        if (!isAcyclic()) {
            throw new IllegalArgumentException("在有环图上无法完成拓扑排序。");
        }

        return inDegreeFirst ? inDegreeFirstResult.getResult() : outDegreeFirstResult.getResult();
    }

    protected void validate() {
        // 入度优先
        inDegreeFirstResult = applySort(node -> node.parentMap.isEmpty(), (map, node) -> {
            node.childMap.keySet().forEach(childId -> {
                map.get(childId).parentMap.remove(node.getId());
            });
        });

        if (!inDegreeFirstResult.isAcyclic) {
            return;
        }

        // 出度优先
        outDegreeFirstResult = applySort(node -> node.childMap.isEmpty(), (map, node) -> {
            node.parentMap.keySet().forEach(parentId -> {
                map.get(parentId).childMap.remove(node.getId());
            });
        });
    }

    private  TopologicSortResult  applySort(Predicate<VertexNode> filter, TopologicSortCallback callback) {
        Map<String, VertexNode> vertexNodeMap = nodeMap.values().stream().map(v -> {
            VertexNode node = new VertexNode(v.delegate);
            node.parentMap.putAll(v.parentMap);
            node.childMap.putAll(v.childMap);
            return node;
        }).collect(Collectors.toMap(VertexNode::getId, v -> v));

        List<List<N>> sortResultList = new ArrayList<>();
        while (!vertexNodeMap.isEmpty()) {
            List<VertexNode> collect = vertexNodeMap.values().stream().filter(filter).collect(Collectors.toList());
            if (collect.isEmpty()) {
                break;
            }
            collect.forEach(node -> {
                callback.apply(vertexNodeMap, node);
                vertexNodeMap.remove(node.getId());
            });

            List<N> resultList = (List<N>) collect.stream().map(v -> v.delegate).collect(Collectors.toList());
            sortResultList.add(resultList);
        }

        TopologicSortResult result =  new TopologicSortResult();
        result.setAcyclic(vertexNodeMap.isEmpty());
        result.setResult(sortResultList);
        return result;
    }


    public static class VertexNode implements IVertex {
        private Map<String, VertexNode> parentMap = new ConcurrentHashMap<>();
        private Map<String, VertexNode> childMap = new ConcurrentHashMap<>();

        private IVertex delegate;
        public VertexNode(IVertex node) {
            delegate = node;
        }

        @Override
        public String getId() {
            return delegate.getId();
        }
    }

    @Setter
    @Getter
    public class TopologicSortResult {
        private boolean isAcyclic;
        private List<List<N>> result;
    }

    public interface TopologicSortCallback {
        void apply(Map<String, VertexNode> vertexNodeMap, VertexNode onNode);
    }
}
