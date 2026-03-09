package com.mude.tod;

import com.mude.tod.dag.Graph;
import com.mude.tod.dag.IVertex;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
public class GraphTest {



    /**
     *       a
     *     /  \
     *    b    c
     *          \
     *           d
     */
    @Test
    public void acyclicGraphTest(){
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        TestNode d = new TestNode("d");

        Graph<IVertex> graph = new Graph<>();
        graph.addEdge(a, b);
        graph.addEdge(a, c);
        graph.addEdge(c, d);

        Assert.isTrue(graph.isAcyclic(), "非有向无环图");
        List<List<IVertex>> inDegreeFirst = graph.topologicalSort(true);
        List<List<IVertex>> outDegreeFirst = graph.topologicalSort(false);
        log.info("inDegreeFirst: {}, outDegreeFirst :{}", inDegreeFirst, outDegreeFirst);
    }

    /**
     *       a
     *     /  \
     *    b    c
     *          \
     *           d
     */
    @Test
    public void cyclicGraphTest(){
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        TestNode d = new TestNode("d");

        Graph<TestNode> graph = new Graph<>();
        graph.addEdge(a, b);
        graph.addEdge(b, c);
        graph.addEdge(c, d);
        graph.addEdge(d, a);

        Assert.isTrue(!graph.isAcyclic(), "有向无环图");
    }

    /**
     *       a      h
     *     /   \
     *    b     c
     *  / | \    \
     * e  f  g    d
     *
     */
    @Test
    public void complexGraphTest(){
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        TestNode d = new TestNode("d");
        TestNode e = new TestNode("e");
        TestNode f = new TestNode("f");
        TestNode g = new TestNode("g");
        TestNode h = new TestNode("h");

        Graph<IVertex> graph = new Graph<>();
        graph.addEdge(a, b);
        graph.addEdge(a, c);
        graph.addEdge(c, d);

        graph.addEdge(b, e);
        graph.addEdge(b, f);
        graph.addEdge(b, g);
        graph.addEdge(h, null);

        Assert.isTrue(graph.isAcyclic(), "非有向无环图");
        List<List<IVertex>> inDegreeFirstResult = graph.topologicalSort(true);
        Assert.isTrue("[a,h][b,c][d,e,f,g]".equals(getSortString(inDegreeFirstResult)), "入度优先排序失败");
        List<List<IVertex>> outDegreeFirstResult = graph.topologicalSort(false);
        Assert.isTrue("[d,e,f,g,h][b,c][a]".equals(getSortString(outDegreeFirstResult)), "出度优先排序失败");
    }


    private String getSortString(List<List<IVertex>> vertices){
        StringBuilder sb = new StringBuilder();

        for (List<IVertex> vertexList : vertices){
            sb.append("[");
            sb.append(join(vertexList));
            sb.append("]");
        }

        return sb.toString();
    }

    private String join(List<IVertex> items) {
        StringBuilder stringBuilder = new StringBuilder();
        for (IVertex item : items) {
            stringBuilder.append(item.getId());
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
