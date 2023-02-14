package Transducer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// class for specifying the transducer in form of a graph. it consists of TransducerNodes and TransducerEdges.
// so far we can add nodes or edges to it and print it.
public class TransducerGraph {

    public Set<TransducerNode> nodes;

    public TransducerGraph() {
        nodes = new HashSet<>();
    }

    public void addTransducerObjectNode(TransducerNode... n) {
        nodes.addAll(Arrays.asList(n));
    }

    public void addTransducerObjectEdge(TransducerNode source, TransducerNode target, String incoming, String outgoing, double cost) {
        nodes.add(source);
        nodes.add(target);

        // don't add duplicate edges!

        for (TransducerEdge edge : source.edges) {
            if (edge.source == source &&
                    edge.target == target &&
                    edge.incomingString.equalsIgnoreCase(incoming) &&
                    edge.outgoingString.equalsIgnoreCase(outgoing) &&
                    edge.cost == cost) {
                return;
            }
        }
        source.edges.add(new TransducerEdge(source, target, incoming, outgoing, cost));

    }

    // TODO: print isolated nodes. (same as in the query)
    public void printGraph() {
        for (TransducerNode node : nodes) {
            for (TransducerEdge edge : node.edges) {
                edge.print();
            }
        }
    }
}
