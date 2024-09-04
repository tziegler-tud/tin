package tin.model.v2.query

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.graph.Node


@SpringBootTest
@TestConfiguration
class QueryGraphTest {

    private val s1 = Node("s1", true, false)
    private val s2 = Node("s2")
    private val s3 = Node("s3")
    private val s4 = Node("s4", false, true)

    private val e0 = QueryEdge(s1, s1, "l");
    private val e1 = QueryEdge(s1, s2, "a");
    private val e11 = QueryEdge(s1, s2, "b");
    private val e2 = QueryEdge(s1, s3, "a");
    private val e3 = QueryEdge(s2, s4, "b")
    private val e4 = QueryEdge(s2, s4, "c")
    private val e5 = QueryEdge(s4, s4, "l")

    fun buildTestGraph() : QueryGraph {
        val graph = QueryGraph();
        graph.addNodes(s1,s2,s3,s4);
        graph.addEdge(e0);
        graph.addEdge(e1);
        graph.addEdge(e11);
        graph.addEdge(e2);
        graph.addEdge(e3);
        graph.addEdge(e4);
        graph.addEdge(e5);
        return graph;
    }

    @Test
    fun testEdgeRetrieval(){


        val graph = buildTestGraph();
        val s1 = graph.getNode("s1")!!
        val s2 = graph.getNode("s2")!!
        val s3 = graph.getNode("s3")!!
        val s4 = graph.getNode("s4")!!

        val edgesS1 = graph.getEdgesWithSource(s1)
        assert(edgesS1.size == 3);
        assert(edgesS1.contains(e0))
        assert(edgesS1.contains(e1))
        assert(edgesS1.contains(e2))

        val edgesTargetS4 = graph.getEdgesWithTarget(s4);
        assert(edgesTargetS4.size == 3)
        assert(edgesTargetS4.contains(e3))
        assert(edgesTargetS4.contains(e4))
        assert(edgesTargetS4.contains(e5))

        val edgesS1S2 = graph.getEdgesWithSourceAndTarget(s1,s2);
        assert(edgesS1S2.size == 2)
        assert(edgesS1S2.contains(e1))
        assert(edgesS1S2.contains(e11))
    }
}