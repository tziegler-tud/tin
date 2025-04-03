package Database;

import java.util.LinkedList;

// class for specifying a database node.
// it only has a name and a list of edges ...
public class DatabaseNode {

    public String identifier;
    public LinkedList<DatabaseEdge> edges;

    public DatabaseNode(){

    }

    public DatabaseNode(String id) {
        identifier = id;
        edges = new LinkedList<>();
    }

}
