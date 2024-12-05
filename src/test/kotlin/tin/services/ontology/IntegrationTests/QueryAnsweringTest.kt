package tin.services.ontology.IntegrationTests

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.QueryReaderServiceV2
import tin.services.internal.fileReaders.TransducerReaderServiceV2
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.ResultGraph.ELHIResultGraphBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPLoopTableBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class QueryAnsweringTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false): FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    fun loadExampleOntology(testOntologyFileName: String): OntologyManager {
        val exampleFile = readWithFileReaderService(testOntologyFileName).get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    private fun readQueryWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<QueryGraph> {
        var fileReaderService: QueryReaderServiceV2 = QueryReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getQueryPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readTransducerWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<TransducerGraph> {
        var fileReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getTransducerPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    @Test
    fun testQueryAnswering() {

        //load ontology
        val manager = loadExampleOntology("pizza_4.rdf")

        val query = readQueryWithFileReaderService("spaCalculation/table/test_comp1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test_comp1.txt")

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
//        ec.prewarmSubsumptionCache()
        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        val spBuilder = ELHISPLoopTableBuilder(query.graph, transducer.graph, manager, ec);

        val startTime = timeSource.markNow()

//        builder.calculateWithDepthLimit(iterationLimit);
        println("Calculating spa table...")

        val spaTable = builder.calculateFullTable();

        val spaEndTime = timeSource.markNow()
        println("Calculating sp table...")
        val spTable = spBuilder.calculateFullTable(spaTable);
        val spEndTime = timeSource.markNow()

        val resultGraphBuilder = ELHIResultGraphBuilder(ec, query.graph ,transducer.graph)
        val resultGraphStartTime = timeSource.markNow()
        val resultGraph = resultGraphBuilder.constructResultGraph(spTable);
        val resultGraphEndTime = timeSource.markNow();

        val prewarmTime = startTime - initialTime;
        val spaTime = spaEndTime - startTime;
        val spTime = spEndTime - spaEndTime;
        val resultGraphTime = resultGraphStartTime - resultGraphEndTime;
        val totalTime = resultGraphEndTime - initialTime;


        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("SPA computation time: " + spaTime)
        println("SP computation time: " + spTime)
        println("ResultGraph computation time: " + resultGraphTime)

        val stats = builder.getExecutionContext().dlReasoner.getStats();

    }
}