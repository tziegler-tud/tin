package Query;

import java.util.LinkedList;

// class for specifying a query node.
// the node has an identifier and is possibly an initialState or a finalState.
public class QueryNode {

    public String identifier;
    public Boolean initialState;
    Boolean finalState;
    public LinkedList<QueryEdge> edges;

    public QueryNode(){

    }

    public QueryNode(String id, Boolean initialState, Boolean finalState) {
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
