package tin.services.internal.fileReaders

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.stereotype.Service
import tin.model.database.DatabaseEdge
import tin.model.database.DatabaseGraph
import tin.model.database.DatabaseNode
import tin.model.query.QueryEdge
import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import tin.services.technical.SystemConfigurationService
import java.nio.file.Path

@SpringBootTest
@TestConfiguration
class QueryReaderServiceTest {

    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    private fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<QueryGraph> {
        var fileReaderService: QueryReaderService = QueryReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getQueryPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun constructComparisonGraph() : QueryGraph {
        //build comparison graph
        val s0 = QueryNode("s0", true, false);
        val s1 = QueryNode("s1", false, false);
        val s2 = QueryNode("s2", false, true);

        val e0 = QueryEdge(s0, s1, "S")
        val e1 = QueryEdge(s1, s0, "R")
        val e2 = QueryEdge(s1, s2, "prop1?")

        val comparisonGraph = QueryGraph();
        comparisonGraph.addNodes(s0,s1,s2);

        comparisonGraph.addEdge(e0.source, e0.target, e0.label);
        comparisonGraph.addEdge(e1.source, e1.target, e1.label);
        comparisonGraph.addEdge(e2.source, e2.target, e2.label);

        comparisonGraph.alphabet.addRoleName("S")
        comparisonGraph.alphabet.addRoleName("R")
        comparisonGraph.alphabet.addConceptName("prop1")

        return comparisonGraph;
    }
    @Test
    fun readSaneFile(){
        val testFileName = "test_query_sane.txt";
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
        val testFileName = "test_query_warning.txt";
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
        assert(warn1.line == "s2, false, true");
    }

    @Test
    fun readFileWithError(){
        val testFileName = "test_query_error.txt";
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
        assert(err0.line == "s2, true, true");

        assert(err1.message == "Failed to read line as node: Invalid input format.");
        assert(err1.index == 9);
        assert(err1.line == "s 3, false, false");

        assert(err2.message == "Failed to read line as node: Invalid input format.");
        assert(err2.index == 11);
        assert(err2.line == "s4, yes, false");

        assert(err3.message == "Failed to read line as node: Invalid input format.");
        assert(err3.index == 13);
        assert(err3.line == "s5?, false, false");

        assert(err4.message == "Failed to read line as edge: Invalid input format.");
        assert(err4.index == 18);
        assert(err4.line == "s 1 , s2, prop1?");

        assert(err5.message == "Failed to read line as edge: Invalid input format.");
        assert(err5.index == 20);
        assert(err5.line == "s1, s2?, prop2?");
    }
}