package tin.services.internal.fileReaders

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v1.database.DatabaseEdge
import tin.model.v1.database.DatabaseGraph
import tin.model.v1.database.DatabaseNode
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.technical.SystemConfigurationService


@SpringBootTest
@TestConfiguration
class DatabaseReaderServiceTest {

    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    private fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<DatabaseGraph> {
        var fileReaderService: DatabaseReaderService = DatabaseReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getDatabasePath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun constructComparisonGraph() : DatabaseGraph {
        //build comparison graph
        val d0 = DatabaseNode("d0");
        val d1 = DatabaseNode("d1");
        val d2 = DatabaseNode("d2");
        val d3 = DatabaseNode("d3");

        val e0 = DatabaseEdge(d0, d1, "l1");
        val e1 = DatabaseEdge(d1, d2, "l2");
        val e2 = DatabaseEdge(d2, d3, "l3");

        val comparisonGraph = DatabaseGraph();
        comparisonGraph.addNodes(d0,d1,d2,d3);

        comparisonGraph.addEdge(d0, d1, "l1");
        comparisonGraph.addEdge(d1, d2, "l2");
        comparisonGraph.addEdge(d2, d3, "l3");

        comparisonGraph.addNodeProperty(d0, "prop1");
        comparisonGraph.addNodeProperty(d1, "prop2");
        comparisonGraph.addNodeProperty(d2, "prop1");
        comparisonGraph.addNodeProperty(d2, "prop2");


        comparisonGraph.alphabet.addRoleName("l1");
        comparisonGraph.alphabet.addRoleName("l2");
        comparisonGraph.alphabet.addRoleName("l3");
        comparisonGraph.alphabet.addConceptName("prop1")
        comparisonGraph.alphabet.addConceptName("prop2")

        return comparisonGraph;
    }
    @Test
    fun readSaneFile(){
        val testFileName = "test_db_sane.txt";
        val result = readWithFileReaderService(testFileName);
        val databaseGraph = result.get();
        val comparisonGraph = constructComparisonGraph();

        //expect no warnings, no errors, and a correct database graph
        assert(result.warnings.isEmpty())
        assert(result.errors.isEmpty())
        assert(databaseGraph == comparisonGraph)
    }

    @Test
    fun readFileWithWarnings(){
        val testFileName = "test_db_warning.txt";
        val result = readWithFileReaderService(testFileName);
        val databaseGraph = result.get();
        val comparisonGraph = constructComparisonGraph();

        //no errors, 3 warnings, and a correct database graph
        assert(result.errors.isEmpty())
        assert(databaseGraph == comparisonGraph)

        //we expect 3 warnings
        assert(result.warnings.size == 3)
        val warn0 = result.warnings[0];
        val warn1 = result.warnings[1];
        val warn2 = result.warnings[2];

        assert(warn0.message == "Unhandled line.");
        assert(warn0.index == 2);
        assert(warn0.line == "unhandled line");

        assert(warn1.message == "Duplicated node identifier.");
        assert(warn1.index == 9);
        assert(warn1.line == "d3");

        assert(warn2.message == "Redundant property assignment: Property 'prop1' already assigned to node 'd2'");
        assert(warn2.index == 19);
        assert(warn2.line == "d2, prop1");
    }

    @Test
    fun readFileWithError(){
        val testFileName = "test_db_error.txt";
        val result = readWithFileReaderService(testFileName);
        val databaseGraph = result.get();
        val comparisonGraph = constructComparisonGraph();

        //no warnings, 5 errors, and a corrupted database graph
        assert(result.warnings.isEmpty())
        assert(databaseGraph !== comparisonGraph)
        //expect 3 warnings
        assert(result.errors.size == 5);

        val err0 = result.errors[0];
        val err1 = result.errors[1];
        val err2 = result.errors[2];
        val err3 = result.errors[3];
        val err4 = result.errors[4];

        assert(err0.message == "Failed to read line as node: Invalid input format.");
        assert(err0.index == 8);
        assert(err0.line == "d4?");

        assert(err1.message == "Failed to read line as node: Invalid input format.");
        assert(err1.index == 10);
        assert(err1.line == "d 5");

        assert(err2.message == "Failed to read line as edge: Invalid input format.");
        assert(err2.index == 15);
        assert(err2.line == "d 1, d3, l4");

        assert(err3.message == "Failed to read line as edge: Invalid input format.");
        assert(err3.index == 18);
        assert(err3.line == "d2, d3, prop3?");

        assert(err4.message == "Failed to read line as property: Invalid input format.");
        assert(err4.index == 24);
        assert(err4.line == "this line creates yet another error");
    }
}