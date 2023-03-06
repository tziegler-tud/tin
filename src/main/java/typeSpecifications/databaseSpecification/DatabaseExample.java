package typeSpecifications.databaseSpecification;

public class DatabaseExample {

    public static void main(String[] args) {
        DatabaseGraph databaseGraph = new DatabaseGraph();
        DatabaseNode zero = new DatabaseNode("0");
        DatabaseNode one = new DatabaseNode("1");
        DatabaseNode two = new DatabaseNode("2");


        databaseGraph.addDatabaseObjectEdge(one, two, "");
        databaseGraph.addDatabaseObjectEdge(one, two, "CAB");
        databaseGraph.addDatabaseObjectEdge(zero, one, "a");
        databaseGraph.addDatabaseObjectEdge(zero, one, "A");
        databaseGraph.addDatabaseObjectEdge(zero, two, "b");

        databaseGraph.printGraph();
    }
}
