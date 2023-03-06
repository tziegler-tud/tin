package typeSpecifications.querySpecification;


import java.util.LinkedList;

// class for specifying a query node.
// the node has an identifier and is possibly an initialState or a finalState.
public class QueryNode {

    private String identifier;
    private boolean initialState;
    private boolean finalState;
    private LinkedList<QueryEdge> edges;

    public QueryNode(String identifier, boolean initialState, boolean finalState) {
        this.identifier = identifier;
        this.initialState = initialState;
        this.finalState = finalState;
        edges = new LinkedList<>();
    }


    public boolean isInitialState() {
        return initialState;
    }

    public boolean isFinalState() {
        return finalState;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LinkedList<QueryEdge> getEdges() {
        return edges;
    }

    public boolean equals(QueryNode otherNode) {
        // compare basic parameters
        if (equalsExcludingEdges(otherNode)) {
            // compare edges
            {
                if (this.edges == null || otherNode.edges == null) {
                    return false;
                }

                if (this.edges.size() != otherNode.edges.size()) {
                    return false;
                }

                return equalsOtherEdgeSet(otherNode.edges);
            }
        }

        return false;
    }

    private boolean equalsOtherEdgeSet(LinkedList<QueryEdge> otherEdgeList) {
        int pairsFound = 0;
        for (QueryEdge thisEdge : this.edges) {
            for (QueryEdge otherEdge : otherEdgeList) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.edges.size());
    }

    public boolean equalsExcludingEdges(QueryNode otherNode) {
        return this.initialState == otherNode.initialState && this.finalState == otherNode.finalState && this.identifier.equals(otherNode.identifier);
    }
}
