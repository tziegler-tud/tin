package typeSpecifications.databaseSpecification;

// class specifying a database edge.
// this edge is now labeled (might be changed later or depending on the database -> use a bool directedGraph (?))
// this edge is a 3-tuple with source and target node and the label of the edge.
public class DatabaseEdge {

    private DatabaseNode source;
    private DatabaseNode target;
    private String label;

    DatabaseEdge(DatabaseNode source, DatabaseNode target, String label) {
        this.source = source;
        this.target = target;
        this.label = label.toLowerCase();
    }

    public String toString() {
        return String.format("(%s) -[%s]-> (%s)", source.getIdentifier(), label, target.getIdentifier());
    }

    public void print() {
        System.out.println(this);
    }

    public boolean equals(DatabaseEdge otherEdge) {
        return this.source.compareToExcludingEdges(otherEdge.getSource()) &&
                this.target.compareToExcludingEdges(otherEdge.getTarget()) &&
                this.label.equals(otherEdge.getLabel());
    }

    public DatabaseNode getSource() {
        return source;
    }

    public DatabaseNode getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }
}
