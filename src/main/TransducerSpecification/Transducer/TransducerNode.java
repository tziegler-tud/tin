package Transducer;

import java.util.LinkedList;

// class for specifying a transducer node.
// similar to the Query.QueryNode we have an identifier and booleans for initial or final states.
public class TransducerNode {

    public String identifier;
    Boolean initialState;
    Boolean finalState;
    public LinkedList<TransducerEdge> edges;

    public TransducerNode(String id, Boolean initialState, Boolean finalState) {
        identifier = id;
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


}
