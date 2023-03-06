package typeSpecifications.productAutomatonSpecification;// this is the class for the productAutomaton consisting of the three base automatons (Query, Transducer, Database) that we specified as graphs in their respective files.

import typeSpecifications.databaseSpecification.DatabaseGraph;
import typeSpecifications.querySpecification.QueryGraph;
import typeSpecifications.transducerSpecification.TransducerGraph;

public class ProductAutomaton {
    QueryGraph queryGraph = new QueryGraph();
    TransducerGraph transducerGraph = new TransducerGraph();
    DatabaseGraph databaseGraph = new DatabaseGraph();

    // dataProvider.fill(example..)

}
