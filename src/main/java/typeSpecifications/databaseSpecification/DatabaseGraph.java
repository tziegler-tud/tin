package typeSpecifications.databaseSpecification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// class for specifying the database in for of a graph. It consists of DatabaseNodes and DatabaseEdges.
// at the moment we only focus on directed databases. (see comment in Database.DatabaseNode.java)
// so far we can add nodes and edges to the database and print it.
public class DatabaseGraph {

    public Set<DatabaseNode> nodes;

    public DatabaseGraph() {
        nodes = new HashSet<>();
    }

    public void addDatabaseObjectNode(DatabaseNode... n) {
        nodes.addAll(Arrays.asList(n));
    }

    public void addDatabaseObjectEdge(DatabaseNode source, DatabaseNode target, String label) {
        nodes.add(source);
        nodes.add(target);

        // don't add duplicate edges!

        for (DatabaseEdge edge : source.getEdges()) {
            if (edge.getSource() == source && edge.getTarget() == target && edge.getLabel().equalsIgnoreCase(label)) {
                return;
            }
        }
        source.getEdges().add(new DatabaseEdge(source, target, label));
    }

    // TODO: print isolated nodes. (same as in query/transducer!)

    public Set<DatabaseNode> getNodes() {
        return nodes;
    }

    public void printGraph() {
        for (DatabaseNode node : nodes) {
            for (DatabaseEdge edge : node.getEdges()) {
                edge.print();
            }
        }
    }

    // only use this for test purposes!
    public boolean equals(DatabaseGraph otherGraph) {
        if (this.nodes == null || otherGraph.nodes == null) {
            return false;
        }

        if (this.nodes.size() != otherGraph.nodes.size()) {
            return false;
        }

        return equalsOtherNodeSet(otherGraph.nodes);
    }

    private boolean equalsOtherNodeSet(Set<DatabaseNode> otherNodeSet) {
        int pairsFound = 0;
        for (DatabaseNode thisNode : this.nodes) {
            for (DatabaseNode otherNode : otherNodeSet) {
                if (thisNode.equals(otherNode)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.nodes.size());
    }
}
