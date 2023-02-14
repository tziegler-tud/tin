package ProductAutomatonSpecification;

import Database.DatabaseNode;
import Query.QueryNode;
import Transducer.TransducerNode;
import org.javatuples.Triplet;

import java.util.LinkedList;

public class ProductAutomatonNode implements Comparable<ProductAutomatonNode> {

    public Triplet<QueryNode, TransducerNode, DatabaseNode> identifier;
    public boolean initialState;
    public boolean finalState;
    public Double weight;
    public LinkedList<ProductAutomatonEdge> edges;

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
        return String.format("(%s, %s, %s)", identifier.getValue0().identifier, identifier.getValue1().identifier, identifier.getValue2().identifier);
    }

    public String toStringWithWeight() {
        if (getWeight() == Double.POSITIVE_INFINITY) {
            return String.format("(%s, %s, %s)[INF]", identifier.getValue0().identifier, identifier.getValue1().identifier, identifier.getValue2().identifier);
        } else
        return String.format("(%s, %s, %s)[%s]", identifier.getValue0().identifier, identifier.getValue1().identifier, identifier.getValue2().identifier, getWeight());
    }

    @Override
    public int compareTo(ProductAutomatonNode productAutomatonNode) {
        return Double.compare(getWeight(), productAutomatonNode.getWeight());
    }
    public String getIdentifier(){
        return String.format("%s|%s|%s", identifier.getValue0().identifier, identifier.getValue1().identifier, identifier.getValue2().identifier);

    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getWeight() {
        return weight;
    }

}
