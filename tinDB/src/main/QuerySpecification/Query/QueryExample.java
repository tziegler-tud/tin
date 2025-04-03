package Query;

public class QueryExample {

    public static void main(String[] args) {
        QueryGraph queryGraph = new QueryGraph();
        QueryNode zero = new QueryNode("0", true, false);
        QueryNode one = new QueryNode("1", false, true);
        QueryNode two = new QueryNode("2", false, false);

        queryGraph.addQueryObjectEdge(zero, one, "A");
        queryGraph.addQueryObjectEdge(zero, one, "a");
        queryGraph.addQueryObjectEdge(zero, two, "B");
        queryGraph.addQueryObjectEdge(two, two, "e");

        queryGraph.printGraph();

    }


}
