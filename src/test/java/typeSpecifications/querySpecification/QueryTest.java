package typeSpecifications.querySpecification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {

    @Test
    void basicTest() {
        QueryGraph queryGraph = new QueryGraph();
        QueryNode node1 = new QueryNode("1", true, false);
        QueryNode node2 = new QueryNode("2", false, true);
        QueryNode node3 = new QueryNode("3", false, false);

        // check if node3 is added via edge
        queryGraph.addQueryNodes(node1, node2);

        queryGraph.addQueryEdge(node1, node3, "edge1");
        queryGraph.addQueryEdge(node3, node2, "edge2");
        queryGraph.addQueryEdge(node3, node3, "edge3");

        assertEquals(3, queryGraph.getNodes().size());
        assertEquals(1, node1.getEdges().size());
        assertEquals(2, node3.getEdges().size());

        // don't add duplicate edge
        queryGraph.addQueryEdge(node3, node3, "edge3");
        assertEquals(2, node3.getEdges().size());

    }

    @Test
    void equalsTest() {
        QueryGraph queryGraph = new QueryGraph();

        QueryNode node1 = new QueryNode("1", true, false);
        QueryNode node2 = new QueryNode("2", false, true);
        QueryNode node3 = new QueryNode("2", false, true);


        queryGraph.addQueryEdge(node1, node2, "abc");
        queryGraph.addQueryEdge(node2, node1, "abc");

        assertFalse(node1.equals(node2));
        assertFalse(node2.equals(node3));

        queryGraph.addQueryEdge(node3, node1, "abc");

        assertTrue(node2.equals(node3));

        QueryEdge queryEdge1 = new QueryEdge(node3, node1, "abc");
        QueryEdge queryEdge2 = new QueryEdge(node3, node1, "abc");
        QueryEdge queryEdge3 = new QueryEdge(node3, node1, "abcdef");

        assertTrue(queryEdge1.equals(queryEdge2));
        assertFalse(queryEdge1.equals(queryEdge3));

    }

}
