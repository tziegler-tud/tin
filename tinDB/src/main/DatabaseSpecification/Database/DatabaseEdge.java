package Database;

// class specifying a database edge.
// this edge is now labeled (might be changed later or depending on the database -> use a bool directedGraph (?))
// this edge is a 3-tuple with source and target node and the label of the edge.
public class DatabaseEdge {

    public DatabaseNode source;
    public DatabaseNode target;
    public String label;

    DatabaseEdge(DatabaseNode source, DatabaseNode target, String label) {
        this.source = source;
        this.target = target;
        this.label = label.toLowerCase();
    }

    public String toString() {
        return String.format("(%s) -[%s]-> (%s)", source.identifier, label, target.identifier);
    }

    public void print() {
        System.out.println(toString());
    }
}
