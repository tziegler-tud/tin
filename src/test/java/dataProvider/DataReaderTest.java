package dataProvider;

import typeSpecifications.databaseSpecification.DatabaseGraph;
import typeSpecifications.databaseSpecification.DatabaseNode;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonConstructor;
import typeSpecifications.productAutomatonSpecification.ProductAutomatonGraph;
import typeSpecifications.querySpecification.QueryGraph;
import typeSpecifications.querySpecification.QueryNode;
import typeSpecifications.transducerSpecification.TransducerGraph;
import typeSpecifications.transducerSpecification.TransducerNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataReaderTest {

    DataReader dataReader;
    DataProvider dataProvider;

    QueryGraph queryGraph;
    TransducerGraph transducerGraph;
    DatabaseGraph databaseGraph;
    ProductAutomatonGraph productAutomatonGraph;
    ProductAutomatonConstructor productAutomatonConstructor;
    HashSet<String> alphabet;
    String datasetIdentifier;

    @BeforeEach
    void setUp() {
        queryGraph = new QueryGraph();
        transducerGraph = new TransducerGraph();
        databaseGraph = new DatabaseGraph();
        productAutomatonGraph = new ProductAutomatonGraph();
        productAutomatonConstructor = null;
        alphabet = new HashSet<>();
        datasetIdentifier = "";
    }

    @Test
    void readFileTest_2006() throws Exception {
        String inputFile2006 = "src/test/resources/data_2006.txt";
        String inputFile2020 = "src/test/resources/data_2020.txt";
        String inputFileEx1 = "src/test/resources/example1.txt";

        dataReader = new DataReader(inputFile2006, false);
        dataReader.readFile();

        dataProvider = dataReader.getDataProvider();

        // build the expected objects
        datasetIdentifier = "data_2006";
        alphabet = Stream.of("R", "S", "T").collect(Collectors.toCollection(HashSet::new));

        queryGraph = queryGraphBuilder(datasetIdentifier);
        transducerGraph = transducerGraphBuilder(datasetIdentifier);
        databaseGraph = databaseGraphBuilder(datasetIdentifier);
        productAutomatonGraph = productAutomatonGraphBuilder(datasetIdentifier);
        productAutomatonConstructor = new ProductAutomatonConstructor(queryGraph, transducerGraph, databaseGraph);
        productAutomatonConstructor.construct();

        assertEquals(alphabet, dataProvider.alphabet);
        assertEquals(datasetIdentifier, dataProvider.dataSetIdentifier);

        assertTrue(queryGraph.equals(dataProvider.getQueryGraph()));
        assertTrue(transducerGraph.equals(dataProvider.getTransducerGraph()));
        assertTrue(databaseGraph.equals(dataProvider.getDatabaseGraph()));

        // todo: implement the expected data for this
        //assertTrue(productAutomatonGraph.equals(productAutomatonConstructor.productAutomatonGraph));
    }

    private QueryGraph queryGraphBuilder(String dataset) {
        QueryGraph queryGraph = new QueryGraph();

        switch (dataset) {
            case "data_2006":
                QueryNode s0 = new QueryNode("s0", true, false);
                QueryNode s1 = new QueryNode("s1", false, true);
                QueryNode s2 = new QueryNode("s2", false, false);
                queryGraph.addQueryNodes(s0, s1, s2);

                queryGraph.addQueryEdge(s0, s2, "S");
                queryGraph.addQueryEdge(s0, s1, "T");
                queryGraph.addQueryEdge(s2, s1, "R");
                return queryGraph;
            case "data_2020":
            default:
                return null;
        }
    }

    private TransducerGraph transducerGraphBuilder(String dataset) {
        TransducerGraph transducerGraph = new TransducerGraph();

        switch (dataset) {
            case "data_2006":
                TransducerNode t0 = new TransducerNode("t0", true, false);
                TransducerNode t1 = new TransducerNode("t1", false, false);
                TransducerNode t2 = new TransducerNode("t2", false, false);
                TransducerNode t3 = new TransducerNode("t3", false, true);

                transducerGraph.addTransducerObjectNode(t0, t1, t2, t3);

                transducerGraph.addTransducerObjectEdge(t0, t1, "R", "R", 0);
                transducerGraph.addTransducerObjectEdge(t1, t2, "T", "S", 2);
                transducerGraph.addTransducerObjectEdge(t2, t3, "T", "R", 0);
                transducerGraph.addTransducerObjectEdge(t2, t3, "T", "S", 1);

                return transducerGraph;
            case "data_2020":
            default:
                return null;
        }
    }

    private DatabaseGraph databaseGraphBuilder(String dataset) {
        DatabaseGraph databaseGraph = new DatabaseGraph();

        switch (dataset) {
            case "data_2006":
                DatabaseNode a = new DatabaseNode("a");
                DatabaseNode b = new DatabaseNode("b");
                DatabaseNode c = new DatabaseNode("c");
                DatabaseNode d = new DatabaseNode("d");
                databaseGraph.addDatabaseObjectNode(a, b, c, d);

                databaseGraph.addDatabaseObjectEdge(a, b, "r");
                databaseGraph.addDatabaseObjectEdge(b, c, "s");
                databaseGraph.addDatabaseObjectEdge(c, d, "r");
                databaseGraph.addDatabaseObjectEdge(d, a, "s");
                databaseGraph.addDatabaseObjectEdge(c, a, "t");

                return databaseGraph;
            case "data_2020":
            default:
                return null;
        }
    }

    private ProductAutomatonGraph productAutomatonGraphBuilder(String dataset) {
        ProductAutomatonGraph productAutomatonGraph = new ProductAutomatonGraph();

        switch (dataset) {
            case "data_2006":

                return productAutomatonGraph;
            case "data_2020":
            default:
                return null;
        }
    }
}
