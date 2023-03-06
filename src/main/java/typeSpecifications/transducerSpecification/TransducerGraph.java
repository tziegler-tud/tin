package typeSpecifications.transducerSpecification;

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

        for (TransducerEdge edge : source.getEdges()) {
            if (edge.getSource() == source &&
                    edge.getTarget() == target &&
                    edge.getIncomingString().equalsIgnoreCase(incoming) &&
                    edge.getOutgoingString().equalsIgnoreCase(outgoing) &&
                    edge.getCost() == cost) {
                return;
            }
        }
        source.getEdges().add(new TransducerEdge(source, target, incoming, outgoing, cost));

    }

    public Set<TransducerNode> getNodes() {
        return nodes;
    }

    // TODO: print isolated nodes. (same as in the query)
    public void printGraph() {
        for (TransducerNode node : nodes) {
            for (TransducerEdge edge : node.getEdges()) {
                edge.print();
            }
        }
    }


    // only use this for test purposes!
    public boolean equals(TransducerGraph otherGraph) {
        if (this.nodes == null || otherGraph.nodes == null) {
            return false;
        }

        if (this.nodes.size() != otherGraph.nodes.size()) {
            return false;
        }

        return equalsOtherNodeSet(otherGraph.nodes);
    }

    private boolean equalsOtherNodeSet(Set<TransducerNode> otherNodeSet) {
        int pairsFound = 0;
        for (TransducerNode thisNode : this.nodes) {
            for (TransducerNode otherNode : otherNodeSet) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.nodes.size());
    }
}
