package tin.model.v2.query

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph


@SpringBootTest
@TestConfiguration
class TransducerGraphTest {

    private val s1 = Node("s1", true, false)
    private val s2 = Node("s2")
    private val s3 = Node("s3")
    private val s4 = Node("s4", false, true)

    private val e0 = TransducerEdge(s1, s1, "prop1", "prop1", 0);
    private val e1 = TransducerEdge(s1, s2, "prop1", "prop2", 1);
    private val e2 = TransducerEdge(s1, s3, "prop1", "prop3", 2);
    private val e3 = TransducerEdge(s2, s4, "prop1", "prop4", 3);
    private val e4 = TransducerEdge(s2, s4, "prop1", "prop1", 0)
    private val e5 = TransducerEdge(s4, s4, "prop2", "prop3", 1);

    fun buildTestGraph1() : TransducerGraph {
        val graph = TransducerGraph();
        graph.addNodes(s1,s2,s3,s4);
        graph.addEdge(e0);
        graph.addEdge(e2);
        graph.addEdge(e3);
        graph.addEdge(e4);
        graph.addEdge(e5);
        return graph;
    }

    fun buildTestGraph2() : TransducerGraph {
        val graph = TransducerGraph();
        graph.addNodes(s1,s2,s3,s4);
        graph.addEdge(e0);
        graph.addEdge(e2);
        graph.addEdge(e3);
        graph.addEdge(e4);
        graph.addEdge(e5);
        return graph;
    }

    @Test
    fun testGraphEquality(){
        val graph1 = buildTestGraph1();
        val graph2 = buildTestGraph2();

        assert(graph1 == graph1)
        graph2.addEdge(e1);
        assert(graph1 != graph2)
    }

    @Test
    fun testEdgeRetrieval(){

        val graph = buildTestGraph1();
        val s1 = graph.getNode("s1")!!
        val s2 = graph.getNode("s2")!!
        val s3 = graph.getNode("s3")!!
        val s4 = graph.getNode("s4")!!

        val edgesS1 = graph.getEdgesWithSource(s1)
        assert(edgesS1.size == 4);
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
    }
}