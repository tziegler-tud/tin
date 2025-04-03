package Database;

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

        for (DatabaseEdge edge : source.edges) {
            if (edge.source == source && edge.target == target && edge.label.equalsIgnoreCase(label)) {
                return;
            }
        }
        source.edges.add(new DatabaseEdge(source, target, label));
    }

    // TODO: print isolated nodes. (same as in query/transducer!)

    public void printGraph() {
        for (DatabaseNode node : nodes) {
            for (DatabaseEdge edge : node.edges) {
                edge.print();
            }
        }
    }
}
