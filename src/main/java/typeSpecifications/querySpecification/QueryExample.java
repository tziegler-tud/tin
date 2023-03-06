package typeSpecifications.querySpecification;

public class QueryExample {

    public static void main(String[] args) {
        QueryGraph queryGraph = new QueryGraph();
        QueryNode zero = new QueryNode("0", true, false);
        QueryNode one = new QueryNode("1", false, true);
        QueryNode two = new QueryNode("2", false, false);

        queryGraph.addQueryEdge(zero, one, "A");
        queryGraph.addQueryEdge(zero, one, "a");
        queryGraph.addQueryEdge(zero, two, "B");
        queryGraph.addQueryEdge(two, two, "e");

        queryGraph.printGraph();

    }


}
