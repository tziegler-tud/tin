package typeSpecifications.productAutomatonSpecification;

import java.util.HashSet;
import java.util.Set;

public class ProductAutomatonGraph {

   public Set<ProductAutomatonNode> nodes;
   public Set<ProductAutomatonNode> initialNodes;
   public Set<ProductAutomatonNode> finalNodes;

    public ProductAutomatonGraph() {
          nodes = new HashSet<>();
          initialNodes = new HashSet<>();
          finalNodes = new HashSet<>();

    }

    /**
     * adds one node to the set of nodes (if it is not already contained)
     * adds the node to the set of initial and final states if it has one of these properties.
     *
     * @param productAutomatonNode the productAutomatonNode
     */
    public void addProductAutomatonNode(ProductAutomatonNode productAutomatonNode) {

        for (ProductAutomatonNode node : nodes) {
            if (node.getIdentifier().getValue0().getIdentifier().equalsIgnoreCase(productAutomatonNode.getIdentifier().getValue0().getIdentifier()) &&
                    node.getIdentifier().getValue1().getIdentifier().equalsIgnoreCase(productAutomatonNode.getIdentifier().getValue1().getIdentifier()) &&
                    node.getIdentifier().getValue2().getIdentifier().equalsIgnoreCase(productAutomatonNode.getIdentifier().getValue2().getIdentifier()))
                return;
        }

        nodes.add(productAutomatonNode);
        if (productAutomatonNode.isInitialState()) {
            initialNodes.add(productAutomatonNode);
        }

        if (productAutomatonNode.isFinalState()) {
            finalNodes.add(productAutomatonNode);
        }
    }

    /**
     * adds one edge to the corresponding node (if it is not already contained)
     *
     * @param source   the source node (productAutomatonNode)
     * @param target   the target node (productAutomatonNode)
     * @param incoming the incoming string
     * @param outgoing the replacement of the incoming string
     * @param cost     the cost of the replacement operation
     */
    public void addProductAutomatonEdge(ProductAutomatonNode source, ProductAutomatonNode target, String incoming, String outgoing, Double cost) {

        // add the nodes of the edge first
        addProductAutomatonNode(source);
        addProductAutomatonNode(target);

        // check whether the edge is already there
        for (ProductAutomatonEdge edge : source.getEdges()) {
            if (edge.getSource() == source && edge.getTarget() == target
                    && edge.getIncomingString().equalsIgnoreCase(incoming)
                    && edge.getOutgoingString().equalsIgnoreCase(outgoing)
                    && edge.getCost() == cost) {
                return;
            }
        }
        // if the edge is new -> add it.
        source.getEdges().add(new ProductAutomatonEdge(source, target, incoming, outgoing, cost));
    }

    /**
     * TODO: refactor print methods into proper .txt file output.
     * prints the graph by going through every node and printing every edge of that node.
     * Use with caution! Bigger graphs might make debugging a little hard.
     */
    public void printGraph(){
        for (ProductAutomatonNode node : nodes) {
            for (ProductAutomatonEdge edge : node.getEdges()) {
                edge.print();
            }
        }
    }


    // only use this for test purposes!
    public boolean equals(ProductAutomatonGraph otherGraph) {
        if (this.nodes == null || otherGraph.nodes == null) {
            return false;
        }

        if (this.nodes.size() != otherGraph.nodes.size()) {
            return false;
        }

        return equalsOtherNodeSet(otherGraph.nodes);
    }

    private boolean equalsOtherNodeSet(Set<ProductAutomatonNode> otherNodeSet) {
        int pairsFound = 0;
        for (ProductAutomatonNode thisNode : this.nodes) {
            for (ProductAutomatonNode otherNode : otherNodeSet) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.nodes.size());
    }


}
