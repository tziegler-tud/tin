package typeSpecifications.transducerSpecification;

import java.util.LinkedList;

// class for specifying a transducer node.
// similar to the Transducer.TransducerNode we have an identifier and booleans for initial or final states.
public class TransducerNode {

    private String identifier;
    private boolean initialState;
    private boolean finalState;
    private  LinkedList<TransducerEdge> edges;

    public TransducerNode(String id, Boolean initialState, Boolean finalState) {
        this.identifier = id;
        this.initialState = initialState;
        this.finalState = finalState;
        this.edges = new LinkedList<>();
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

    public LinkedList<TransducerEdge> getEdges() {
        return edges;
    }

    public boolean equals(TransducerNode otherNode) {
        // compare basic parameters
        if (compareToExcludingEdges(otherNode)) {
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

    private boolean equalsOtherEdgeSet(LinkedList<TransducerEdge> otherEdgeList) {
        int pairsFound = 0;
        for (TransducerEdge thisEdge : this.edges) {
            for (TransducerEdge otherEdge : otherEdgeList) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.edges.size());
    }

    public boolean compareToExcludingEdges(TransducerNode otherNode) {
        return this.initialState == otherNode.initialState && this.finalState == otherNode.finalState && this.identifier.equals(otherNode.identifier);
    }
}
