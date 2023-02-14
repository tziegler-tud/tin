package ProductAutomatonSpecification;

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
            if (node.identifier.getValue0().identifier.equalsIgnoreCase(productAutomatonNode.identifier.getValue0().identifier) &&
                    node.identifier.getValue1().identifier.equalsIgnoreCase(productAutomatonNode.identifier.getValue1().identifier) &&
                    node.identifier.getValue2().identifier.equalsIgnoreCase(productAutomatonNode.identifier.getValue2().identifier))
                return;
        }

        nodes.add(productAutomatonNode);
        if (productAutomatonNode.initialState) {
            initialNodes.add(productAutomatonNode);
        }

        if (productAutomatonNode.finalState) {
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
        for (ProductAutomatonEdge edge : source.edges) {
            if (edge.source == source && edge.target == target
                    && edge.incomingString.equalsIgnoreCase(incoming)
                    && edge.outgoingString.equalsIgnoreCase(outgoing)
                    && edge.cost == cost) {
                return;
            }
        }
        // if the edge is new -> add it.
        source.edges.add(new ProductAutomatonEdge(source, target, incoming, outgoing, cost));
    }

    /**
     * TODO: refactor print methods into proper .txt file output.
     * prints the graph by going through every node and printing every edge of that node.
     * Use with caution! Bigger graphs might make debugging a little hard.
     */
    public void printGraph(){
        for (ProductAutomatonNode node : nodes) {
            for (ProductAutomatonEdge edge : node.edges ) {
                edge.print();
            }
        }
    }


}
