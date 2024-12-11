package tin.services.ontology.IntegrationTests.LUBM

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
import tin.services.internal.utils.DLTransducerFactory
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.ResultGraph.DijkstraSolver
import tin.services.ontology.ResultGraph.ELResultGraphBuilder
import tin.services.ontology.ResultGraph.FloydWarshallSolver
import tin.services.ontology.ResultGraph.ResultGraphSolver
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPLoopTableBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class LUBMTest {
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

    private fun readQueryWithFileReaderService(
        fileName: String,
        breakOnError: Boolean = false
    ): FileReaderResult<QueryGraph> {
        var fileReaderService: QueryReaderServiceV2 = QueryReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getQueryPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readTransducerWithFileReaderService(
        fileName: String,
        breakOnError: Boolean = false
    ): FileReaderResult<TransducerGraph> {
        var fileReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getTransducerPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    @Test
    fun runLUBMExample() {
        val manager = loadExampleOntology("LUBM/merged/merge_0_0.rdf");
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH, false);

        val query = readQueryWithFileReaderService("LUBM/univbench-test1.txt")

//        val transducer = readTransducerWithFileReaderService("LUBM/univbench-test1.txt")
//        val transducerGraph = transducer.graph
        val transducerGraph = DLTransducerFactory.generateClassicAnswersTransducer(ec);

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
//        ec.prewarmSubsumptionCache()
        val builder = ELSPALoopTableBuilder(query.graph, transducerGraph, manager, ec);
        val spBuilder = ELSPLoopTableBuilder(query.graph, transducerGraph, manager, ec);

        val startTime = timeSource.markNow()

//        builder.calculateWithDepthLimit(iterationLimit);
        println("Calculating spa table...")

        val spaTable = builder.calculateFullTable();

        val spaEndTime = timeSource.markNow()
        println("Calculating sp table...")
        val spTable = spBuilder.calculateFullTable(spaTable);
        val spEndTime = timeSource.markNow()


        val prewarmTime = startTime - initialTime;
        val spaTime = spaEndTime - startTime;
        val spTime = spEndTime - spaEndTime;
        val totalTime = spEndTime - initialTime;


        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("SPA computation time: " + spaTime)
        println("SP computation time: " + spTime)

        val stats = builder.getExecutionContext().dlReasoner.getStats();

        println("Superclass Cache Size: " + stats["superClassCache"])
        println("Superclass Cache Hits: " + stats["superClassCacheHitCounter"])

        println("Equiv Node Cache Size: " + stats["equivalentClassCache"])
        println("Equiv Node Cache Hits: " + stats["equivNodeCacheHitCounter"])

        println("SubClasses Cache Size: " + stats["subClassCache"])
        println("SubClasses Cache Hits: " + stats["subClassCacheHitCounter"])

        println("Entailment Check Cache Size: " + stats["entailmentCache"])
        println("Entailment Cache Hits: " + stats["entailmentCacheHitCounter"])
        println("Entailment Cache Misses: " + stats["entailmentCacheMissCounter"])

        println("Results:")
        spTable.map.forEach { (key, value) ->
            println(key.transformToString(ec.shortFormProvider) + ": " + value)
        }

        val resultBuilder = ELResultGraphBuilder(ec, query.graph, transducerGraph)
        val resultGraph = resultBuilder.constructResultGraph(spTable)
        val fwSolver = FloydWarshallSolver(resultGraph);
        val dijkstraSolver = DijkstraSolver(resultGraph);

        val allPaths = fwSolver.getAllShortestPaths();
        assert(allPaths.isNotEmpty())

    }
}