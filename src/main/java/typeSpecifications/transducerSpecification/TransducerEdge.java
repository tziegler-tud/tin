package typeSpecifications.transducerSpecification;

// class specifying a transducer edge.
// this edge is more special than the standard query edge.
// we need a 5-tuple, with source and target nodes, incoming and outgoing strings and a cost (doing this replacement operation)
public class TransducerEdge {

    private TransducerNode source;
    private TransducerNode target;
    private String incomingString;
    private String outgoingString;
    private double cost;

    TransducerEdge(TransducerNode source, TransducerNode target, String incomingString, String outgoingString, double cost) {
        this.source = source;
        this.target = target;
        this.incomingString = incomingString.toLowerCase();
        this.outgoingString = outgoingString.toLowerCase();
        this.cost = cost;
    }

    public String toString() {

        String eps = "ε";
        String incoming;
        String outgoing;

        if (incomingString.isEmpty()) {
            incoming = eps;
        } else incoming = incomingString;

        if (outgoingString.isEmpty()) {
            outgoing = eps;
        } else outgoing = outgoingString;

        return String.format("(%s) -[%3s|%3s|%3s]-> (%s)", source.getIdentifier(), incoming, outgoing, cost, target.getIdentifier());
    }

    public void print() {
        System.out.println(this);
    }

    public boolean equals(TransducerEdge otherEdge) {
        return this.source.compareToExcludingEdges(otherEdge.getSource()) &&
                this.target.compareToExcludingEdges(otherEdge.getTarget()) &&
                this.getIncomingString().equals(otherEdge.getIncomingString()) &&
                this.getOutgoingString().equals(otherEdge.getOutgoingString()) &&
                this.getCost() == otherEdge.getCost();
    }

    public TransducerNode getSource() {
        return source;
    }

    public TransducerNode getTarget() {
        return target;
    }

    public String getIncomingString() {
        return incomingString;
    }

    public String getOutgoingString() {
        return outgoingString;
    }

    public double getCost() {
        return cost;
    }
}
