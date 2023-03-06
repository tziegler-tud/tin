package typeSpecifications.querySpecification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// class for specifying the query in form of a graph. it consists of QueryNodes and QueryEdges.
// so far we can add nodes or edges to it and print it.
public class QueryGraph {
    private Set<QueryNode> nodes;

    public QueryGraph() {
        nodes = new HashSet<>();
    }

    public void addQueryNodes(QueryNode... n) {
        nodes.addAll(Arrays.asList(n));
    }

    public void addQueryEdge(QueryNode source, QueryNode target, String label) {

        // .add on a hashSet does not add an element that is already present! We don't need to check whether the node is already created.

        nodes.add(source);
        nodes.add(target);

        // if this edge is already present -> don't add it.

        for (QueryEdge edge : source.getEdges()) {
            if (edge.getSource() == source && edge.getTarget() == target && edge.getLabel().equalsIgnoreCase(label)) {
                return;
            }
        }
        source.getEdges().add(new QueryEdge(source, target, label));
    }

    public void printGraph() {
        for (QueryNode node : nodes) {
            for (QueryEdge edge : node.getEdges()) {
                edge.print();
            }
        }
    }

    public Set<QueryNode> getNodes() {
        return nodes;
    }


    // only use this for test purposes!
    public boolean equals(QueryGraph otherGraph) {
        if (this.nodes == null || otherGraph.nodes == null) {
            return false;
        }

        if (this.nodes.size() != otherGraph.nodes.size()) {
            return false;
        }

        return equalsOtherNodeSet(otherGraph.nodes);
    }

    private boolean equalsOtherNodeSet(Set<QueryNode> otherNodeSet) {
        int pairsFound = 0;
        for (QueryNode thisNode : this.nodes) {
            for (QueryNode otherNode : otherNodeSet) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.nodes.size());
    }
}
