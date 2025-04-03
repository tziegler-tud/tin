package Query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// class for specifying the query in form of a graph. it consists of QueryNodes and QueryEdges.
// so far we can add nodes or edges to it and print it.
public class QueryGraph {
    public Set<QueryNode> nodes;

    public QueryGraph() {
        nodes = new HashSet<>();
    }

    public void addQueryObjectNode(QueryNode... n) {
        nodes.addAll(Arrays.asList(n));
    }

    public void addQueryObjectEdge(QueryNode source, QueryNode target, String label) {

        // .add on a hashSet does not add an element that is already present! We don't need to check whether the node is already created.

        nodes.add(source);
        nodes.add(target);

        // if this edge is already present -> don't add it.

        for (QueryEdge edge : source.edges) {
            if (edge.source == source && edge.target == target && edge.label.equalsIgnoreCase(label)) {
                return;
            }
        }
        source.edges.add(new QueryEdge(source, target, label));
    }
    public Set<QueryNode> getNodes(){
        return nodes;
    }

    // TODO: add isolated nodes! (same as in the transducer)
    public void printGraph() {
        for (QueryNode node : nodes) {
            for (QueryEdge edge : node.edges) {
                edge.print();
            }
        }
    }
}
