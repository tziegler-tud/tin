package typeSpecifications.productAutomatonSpecification;

public class ProductAutomatonEdge {

    private ProductAutomatonNode source;
    private ProductAutomatonNode target;
    private String incomingString;
    private String outgoingString;
    private double cost;

    /**
     * ProductAutomatonEdges here have the following form
     * (source) -[incomingString/outgoingString/cost]-> (target)
     *
     * @param source         the source node (a ProductAutomatonSpecification.ProductAutomatonNode)
     * @param target         the target node (a ProductAutomatonSpecification.ProductAutomatonNode)
     * @param incomingString the string we (as a transducer) read
     * @param outgoingString the string we (as a transducer) return as a replacement
     * @param cost           the cost of the replacement
     */
    ProductAutomatonEdge(ProductAutomatonNode source, ProductAutomatonNode target, String incomingString, String outgoingString, double cost) {
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

        return String.format("(%s, %s, %s) -[%3s, %3s, %3s]-> (%s, %s, %s)",
                source.getIdentifier().getValue0().getIdentifier(), source.getIdentifier().getValue1().getIdentifier(), source.getIdentifier().getValue2().getIdentifier(),
                incoming, outgoing, cost,
                target.getIdentifier().getValue0().getIdentifier(), target.getIdentifier().getValue1().getIdentifier(), target.getIdentifier().getValue2().getIdentifier()
        );
    }

    public void print() {
        System.out.println(toString());
    }

    public boolean equals(ProductAutomatonEdge otherEdge) {
        return this.getSource().equalsExcludingEdges(otherEdge.getSource()) &&
                this.getTarget().equalsExcludingEdges(otherEdge.getTarget()) &&
                this.getIncomingString().equals(otherEdge.getIncomingString()) &&
                this.getOutgoingString().equals(otherEdge.getOutgoingString()) &&
                this.getCost() == otherEdge.getCost();
    }

    public ProductAutomatonNode getSource() {
        return source;
    }

    public ProductAutomatonNode getTarget() {
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
