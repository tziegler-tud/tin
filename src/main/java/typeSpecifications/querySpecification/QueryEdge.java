package typeSpecifications.querySpecification;


// class for specifying a query edge.
// A node is labeled and has a source (Query.QueryNode object) and destination (Query.QueryNode object)
// all labels are stored in lowercase to prevent casing-errors!
// using uppercase labels doesn't throw an error, but it'll still get stored in lowercase.
public class QueryEdge {

    private final QueryNode source;
    private final QueryNode target;
    private final String label;

    public QueryEdge(QueryNode source, QueryNode target, String label) {
        this.source = source;
        this.target = target;
        this.label = label.toLowerCase();
    }

    public String toString() {

        String eps = "ε";
        String incoming;

        if (label.isEmpty()) {
            incoming = eps;
        } else incoming = label;

        return String.format("(%s) -[%3s]-> (%s)", source.getIdentifier(), incoming, target.getIdentifier());
    }

    public void print() {
        System.out.println(this);
    }

    public QueryNode getSource() {
        return source;
    }

    public QueryNode getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }

    public boolean equals(QueryEdge otherEdge) {
        return this.source.equalsExcludingEdges(otherEdge.source) &&
                this.target.equalsExcludingEdges(otherEdge.target) &&
                this.label.equals(otherEdge.label);
    }
}
