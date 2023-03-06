package typeSpecifications.productAutomatonSpecification;

import typeSpecifications.databaseSpecification.DatabaseNode;
import typeSpecifications.querySpecification.QueryNode;
import typeSpecifications.transducerSpecification.TransducerNode;
import org.javatuples.Triplet;

import java.util.LinkedList;

public class ProductAutomatonNode {

    private Triplet<QueryNode, TransducerNode, DatabaseNode> identifier;
    private boolean initialState;
    private boolean finalState;
    private Double weight;
    private LinkedList<ProductAutomatonEdge> edges;

    public ProductAutomatonNode() {

    }

    /**
     * constructor for a productAutomatonNode. it has the form
     * (queryNode, transducerNode, databaseNode) and has booleans for being an initial or final state
     *
     * @param qNode        the corresponding queryNode
     * @param tNode        the corresponding transducerNode
     * @param dNode        the corresponding databaseNode
     * @param initialState boolean for being an initial state
     * @param finalState   boolean for being a final state
     */
    public ProductAutomatonNode(QueryNode qNode, TransducerNode tNode, DatabaseNode dNode, Boolean initialState, Boolean finalState) {
        identifier = Triplet.with(qNode, tNode, dNode);
        this.initialState = initialState;
        this.finalState = finalState;
        setWeight(null);
        edges = new LinkedList<>();

    }

    /**
     * simple print method.
     */
    public void print() {
        System.out.println(toString());
    }

    public void printWithWeight() {
        System.out.println(toStringWithWeight());
    }

    public String toString() {
        return String.format("(%s, %s, %s)", identifier.getValue0().getIdentifier(), identifier.getValue1().getIdentifier(), identifier.getValue2().getIdentifier());
    }

    public String toStringWithWeight() {
        if (getWeight() == Double.POSITIVE_INFINITY) {
            return String.format("(%s, %s, %s)[INF]", identifier.getValue0().getIdentifier(), identifier.getValue1().getIdentifier(), identifier.getValue2().getIdentifier());
        } else
            return String.format("(%s, %s, %s)[%s]", identifier.getValue0().getIdentifier(), identifier.getValue1().getIdentifier(), identifier.getValue2().getIdentifier(), getWeight());
    }


    public String getIdentifierString() {
        return String.format("%s|%s|%s", identifier.getValue0().getIdentifier(), identifier.getValue1().getIdentifier(), identifier.getValue2().getIdentifier());
    }

    public Triplet<QueryNode, TransducerNode, DatabaseNode> getIdentifier() {
        return identifier;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getWeight() {
        return weight;
    }

    public boolean isInitialState() {
        return initialState;
    }

    public boolean isFinalState() {
        return finalState;
    }

    public LinkedList<ProductAutomatonEdge> getEdges() {
        return edges;
    }

    public boolean equals(ProductAutomatonNode otherNode) {
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

    private boolean equalsOtherEdgeSet(LinkedList<ProductAutomatonEdge> otherEdgeList) {
        int pairsFound = 0;
        for (ProductAutomatonEdge thisEdge : this.edges) {
            for (ProductAutomatonEdge otherEdge : otherEdgeList) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.edges.size());
    }

    public boolean equalsExcludingEdges(ProductAutomatonNode otherNode) {
        return this.initialState == otherNode.initialState && this.finalState == otherNode.finalState && this.getWeight().equals(otherNode.getWeight()) && checkIdentifierEquality(otherNode);
    }

    private boolean checkIdentifierEquality(ProductAutomatonNode otherNode) {
        return this.identifier.getValue0().equals(otherNode.identifier.getValue0()) &&
                this.identifier.getValue1().equals(otherNode.identifier.getValue1()) &&
                this.identifier.getValue2().equals(otherNode.identifier.getValue2());
    }

}
