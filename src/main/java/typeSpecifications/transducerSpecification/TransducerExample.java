package typeSpecifications.transducerSpecification;

public class TransducerExample {

    public static void main(String[] args) {
        TransducerGraph transducerGraph = new TransducerGraph();
        TransducerNode zero = new TransducerNode("0", true, false);
        TransducerNode one = new TransducerNode("1", false, false);
        TransducerNode two = new TransducerNode("2", false, false);
        TransducerNode three = new TransducerNode("3", false, true);

        transducerGraph.addTransducerObjectEdge(zero, one, "A", "B", 1);
        transducerGraph.addTransducerObjectEdge(one, two, "A", "b", 2);
        transducerGraph.addTransducerObjectEdge(two, three, "a", "", 1);
        transducerGraph.addTransducerObjectEdge(two, three, "A", "", 1);

        transducerGraph.printGraph();
    }
}
