package typeSpecifications.databaseSpecification;

import java.util.LinkedList;

// class for specifying a database node.
// it only has a name and a list of edges ...
public class DatabaseNode  {

    private String identifier;
    private LinkedList<DatabaseEdge> edges;

    public DatabaseNode() {

    }

    public DatabaseNode(String id) {
        identifier = id;
        edges = new LinkedList<>();
    }

    public String getIdentifier() {
        return identifier;
    }

    public LinkedList<DatabaseEdge> getEdges() {
        return edges;
    }

    public boolean equals(DatabaseNode otherNode) {
        // compare basic parameters
        if (compareToExcludingEdges(otherNode)) {
            // compare edges
            {
                if (this.edges == null || otherNode.edges == null) {
                    return false;
                }

                if (this.edges.size() != otherNode.edges.size()) {
                    return false;
                }

                return equalsOtherEdgeSet(otherNode.edges);
            }
        }

        return false;
    }

    private boolean equalsOtherEdgeSet(LinkedList<DatabaseEdge> otherEdgeList) {
        int pairsFound = 0;
        for (DatabaseEdge thisEdge : this.edges) {
            for (DatabaseEdge otherEdge : otherEdgeList) {
                if (thisEdge.equals(otherEdge)) {
                    pairsFound++;
                    break;
                }
            }
        }
        return pairsFound == (this.edges.size());
    }

    public boolean compareToExcludingEdges(DatabaseNode otherNode) {
        return this.identifier.equals(otherNode.identifier);
    }



}
