package tin.services.internal.fileReaders

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.model.v2.graph.Node
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.technical.SystemConfigurationService

@SpringBootTest
@TestConfiguration
class TransducerReaderServiceV2Test {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    private fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<TransducerGraph> {
        var fileReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getTransducerPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun constructComparisonGraph() : TransducerGraph {
        //build comparison graph
        val t0 = Node("t0", true, false);
        val t1 = Node("t1", false, false);
        val t2 = Node("t2", false, true);

        val e0 = TransducerEdge(t0, t1, "R", "R", 0)
        val e1 = TransducerEdge(t1, t2, "T", "S", 2)
        val e2 = TransducerEdge(t0, t2, "prop1?", "prop2?", 2)
        val e3 = TransducerEdge(t2, t2, "R", "prop1?", 4)

        val comparisonGraph = TransducerGraph();
        comparisonGraph.addNodes(t0,t1,t2);

        comparisonGraph.addEdge(e0.source, e0.target, e0.incomingString, e0.outgoingString, e0.cost);
        comparisonGraph.addEdge(e1.source, e1.target, e1.incomingString, e1.outgoingString, e1.cost);
        comparisonGraph.addEdge(e2);
        comparisonGraph.addEdge(e3);

        comparisonGraph.alphabet.addRoleName("R")
        comparisonGraph.alphabet.addRoleName("S")
        comparisonGraph.alphabet.addRoleName("T")
        comparisonGraph.alphabet.addConceptName("prop1")
        comparisonGraph.alphabet.addConceptName("prop2")

        return comparisonGraph;
    }
    @Test
    fun readSaneFile(){
        val testFileName = "test_transducer_sane.txt";
        val result = readWithFileReaderService(testFileName);
        val graph = result.get();
        val comparisonGraph = constructComparisonGraph();

        //expect no warnings, no errors, and a correct database graph
        assert(result.warnings.isEmpty())
        assert(result.errors.isEmpty())
        assert(graph == comparisonGraph)
    }

    @Test
    fun readFileWithWarnings(){
        val testFileName = "test_transducer_warning.txt";
        val result = readWithFileReaderService(testFileName);
        val graph = result.get();
        val comparisonGraph = constructComparisonGraph();

        //no errors, 3 warnings, and a correct database graph
        assert(result.errors.isEmpty())
        assert(graph == comparisonGraph)

        //we expect 3 warnings
        assert(result.warnings.size == 2)
        val warn0 = result.warnings[0];
        val warn1 = result.warnings[1];

        assert(warn0.message == "Unhandled line.");
        assert(warn0.index == 3);
        assert(warn0.line == "unhandled line");

        assert(warn1.message == "Duplicated node identifier.");
        assert(warn1.index == 9);
        assert(warn1.line == "t2, false, true");
    }

    @Test
    fun readFileWithError(){
        val testFileName = "test_transducer_error.txt";
        val result = readWithFileReaderService(testFileName);
        val graph = result.get();
        val comparisonGraph = constructComparisonGraph();

        //no warnings, 5 errors, and a corrupted database graph
        assert(result.warnings.isEmpty())
        assert(graph !== comparisonGraph)
        //expect 3 warnings
        assert(result.errors.size == 6);

        val err0 = result.errors[0];
        val err1 = result.errors[1];
        val err2 = result.errors[2];
        val err3 = result.errors[3];
        val err4 = result.errors[4];
        val err5 = result.errors[5];

        assert(err0.message == "Failed to read line as node: Non-repairable duplicated node identifier.");
        assert(err0.index == 7);
        assert(err0.line == "t2, true, true");

        assert(err1.message == "Failed to read line as node: Invalid input format.");
        assert(err1.index == 9);
        assert(err1.line == "t 3, false, false");

        assert(err2.message == "Failed to read line as node: Invalid input format.");
        assert(err2.index == 11);
        assert(err2.line == "t4, yes, false");

        assert(err3.message == "Failed to read line as node: Invalid input format.");
        assert(err3.index == 13);
        assert(err3.line == "t5?, false, false");

        assert(err4.message == "Failed to read line as edge: Invalid input format.");
        assert(err4.index == 20);
        assert(err4.line == "t 1 , t2, prop1?, prop2?, 1");

        assert(err5.message == "Failed to read line as edge: Invalid input format.");
        assert(err5.index == 22);
        assert(err5.line == "t1, t2?, R, S, 3");
    }
}