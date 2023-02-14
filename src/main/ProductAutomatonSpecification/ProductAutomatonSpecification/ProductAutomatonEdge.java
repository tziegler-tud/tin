package ProductAutomatonSpecification;

public class ProductAutomatonEdge {

    public ProductAutomatonNode source;
    public  ProductAutomatonNode target;
    public String incomingString;
    public String outgoingString;
    public double cost;

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

        if (incomingString.isEmpty()){
            incoming = eps;
        } else incoming = incomingString;

        if (outgoingString.isEmpty()){
            outgoing = eps;
        } else outgoing = outgoingString;

        return String.format("(%s, %s, %s) -[%3s, %3s, %3s]-> (%s, %s, %s)",
                source.identifier.getValue0().identifier, source.identifier.getValue1().identifier, source.identifier.getValue2().identifier,
                incoming, outgoing, cost,
                target.identifier.getValue0().identifier, target.identifier.getValue1().identifier, target.identifier.getValue2().identifier
        );
    }

    public void print() {
        System.out.println(toString());
    }
}
