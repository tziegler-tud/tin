package queryAnswering.algorithms;

import typeSpecifications.productAutomatonSpecification.ProductAutomatonEdge;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonGraph;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonNode;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Set;

public class DijkstraUtils {

    public static HashMap<Pair<String, String>, Double> retrieveResultForOneInitialNode(ProductAutomatonNode initialNode, Set<ProductAutomatonNode> setOfNodes) {

        HashMap<Pair<String, String>, Double> answerMap = new HashMap<>();

        // for all nodes in set S
        for (ProductAutomatonNode node : setOfNodes) {
            // if you are a final state and your weight is not infinite (that means there is a path from the source to you)
            if (node.isFinalState() && !node.getWeight().isInfinite()) {
                // I add you to the final answerSet in the form of ((source, target), weight)
                Pair<String, String> answerPair = new Pair<>(initialNode.getIdentifier().getValue2().getIdentifier(), node.getIdentifier().getValue2().getIdentifier());
                answerMap.put(answerPair, node.getWeight());
            }
        }

        return answerMap;
    }

    public static void relax(ProductAutomatonNode u, ProductAutomatonNode v, ProductAutomatonEdge edge, HashMap<ProductAutomatonNode, ProductAutomatonNode> predecessor) {

        Double newCost = u.getWeight() + edge.getCost();

        // line 8.1
        if (v.getWeight() >= newCost && !newCost.isInfinite()) {

            // line 8.2
            v.setWeight(newCost);

            // line 8.3
            // predecessor of v is u.
            predecessor.put(v, u);
        }
    }

    public static void initialiseSingleSource(ProductAutomatonGraph graph, ProductAutomatonNode sourceNode, HashMap<ProductAutomatonNode, ProductAutomatonNode> predecessor) {

        // line 1.1
        for (ProductAutomatonNode node : graph.nodes) {

            // line 1.2
            node.setWeight(Double.POSITIVE_INFINITY);

            // line 1.3
            predecessor.put(node, null);
        }
        // line 1.4
        sourceNode.setWeight(0.0);
    }
}
