package tinDL.services.internal.utils

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.services.internal.utils.TransducerFactory

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
        val t0 = Node("t0", true, true);

        var transducer = TransducerGraph();
        transducer.addNodes(t0);

        transducer.addEdge(TransducerEdge(t0, t0, "test1", "toast1", 2))
        transducer.addEdge(TransducerEdge(t0, t0, "test1", "test9", 1))
        transducer.addEdge(TransducerEdge(t0, t0, "test1", "chair", 5))

        transducer.addEdge(TransducerEdge(t0, t0, "test9", "toast1", 3))
        transducer.addEdge(TransducerEdge(t0, t0, "test9", "test9", 0))
        transducer.addEdge(TransducerEdge(t0, t0, "test9", "chair", 5))

        transducer.addEdge(TransducerEdge(t0, t0, "chain", "toast1", 5))
        transducer.addEdge(TransducerEdge(t0, t0, "chain", "test9", 5))
        transducer.addEdge(TransducerEdge(t0, t0, "chain", "chair", 1))

        transducer.addEdge(TransducerEdge(t0,t0,"haveStudent", "hasStudent", 2))
        transducer.addEdge(TransducerEdge(t0,t0,"haveStudent", "hasEmployee", 9))

        transducer.addEdge(TransducerEdge(t0,t0,"hasEmployer", "hasStudent", 8))
        transducer.addEdge(TransducerEdge(t0,t0,"hasEmployer", "hasEmployee", 1))

        return transducer;
    }
}