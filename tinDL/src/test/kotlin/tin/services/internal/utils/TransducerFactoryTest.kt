package tin.services.internal.utils

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v1.alphabet.Alphabet
import tin.model.v1.transducer.TransducerEdge
import tin.model.v1.transducer.TransducerGraph
import tin.model.v1.transducer.TransducerNode

@SpringBootTest
@TestConfiguration
class TransducerFactoryTest {

    private fun getTestQueryAlphabet() : Alphabet {
        var alphabet = Alphabet();
        alphabet.addConceptName("test1");
        alphabet.addConceptName("test9");
        alphabet.addConceptName("chain");
        alphabet.addRoleName("haveStudent");
        alphabet.addRoleName("hasEmployer");
        return alphabet
    }

    private fun getTestDatabaseAlphabet() : Alphabet {
        var alphabet = Alphabet();
        alphabet.addConceptName("toast1");
        alphabet.addConceptName("test9");
        alphabet.addConceptName("chair");
        alphabet.addRoleName("hasStudent");
        alphabet.addRoleName("hasEmployee");
        return alphabet
    }

    private fun constructComparisonGraph() : TransducerGraph {
        //build comparison graph
        val t0 = TransducerNode("t0", true, true);

        var transducer = TransducerGraph();
        transducer.addNodes(t0);

        transducer.addEdge(TransducerEdge(t0, t0, "test1", "toast1", 2.0))
        transducer.addEdge(TransducerEdge(t0, t0, "test1", "test9", 1.0))
        transducer.addEdge(TransducerEdge(t0, t0, "test1", "chair", 5.0))

        transducer.addEdge(TransducerEdge(t0, t0, "test9", "toast1", 3.0))
        transducer.addEdge(TransducerEdge(t0, t0, "test9", "test9", 0.0))
        transducer.addEdge(TransducerEdge(t0, t0, "test9", "chair", 5.0))

        transducer.addEdge(TransducerEdge(t0, t0, "chain", "toast1", 5.0))
        transducer.addEdge(TransducerEdge(t0, t0, "chain", "test9", 5.0))
        transducer.addEdge(TransducerEdge(t0, t0, "chain", "chair", 1.0))

        transducer.addEdge(TransducerEdge(t0,t0,"haveStudent", "hasStudent", 2.0))
        transducer.addEdge(TransducerEdge(t0,t0,"haveStudent", "hasEmployee", 9.0))

        transducer.addEdge(TransducerEdge(t0,t0,"hasEmployer", "hasStudent", 8.0))
        transducer.addEdge(TransducerEdge(t0,t0,"hasEmployer", "hasEmployee", 1.0))

        return transducer;
    }

    @Test
    fun testEditDistanceTransducer() {
        val queryAlphabet = getTestQueryAlphabet();
        val databaseAlphabet = getTestDatabaseAlphabet();

        val transducer = TransducerFactory.generateEditDistanceTransducer(queryAlphabet, databaseAlphabet);

        val comparisonTransducer = constructComparisonGraph();

        val edges = comparisonTransducer.getNode("t0")!!.edges;

        for(node in transducer.nodes) {
            for (edge in node.edges) {
                println("Checking edge: " + edge.toString());
                assert(edges.contains(edge));
            }
        }

        assert(transducer == comparisonTransducer);

    }
}