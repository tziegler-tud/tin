package tin.services.ontology.LoopTableBuilder

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.DLReasoner
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.SPALoopTableBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class SpaLoopTableTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
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

    fun loadExampleOntology() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza2.rdf").get()
//        val exampleFile = readWithFileReaderService("univ-bench.owl.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testLoopTableInitialStep() {
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("test1.txt")
        val transducer = readTransducerWithFileReaderService("test1.txt")

        val builder = SPALoopTableBuilder(query.graph, transducer.graph, manager);
        builder.calculateInitialStep();
        assert(true)
    }

    @Test
    fun testLoopTableConstruction(){
        val manager = loadExampleOntology();



        val query = readQueryWithFileReaderService("spaCalculation/table/test1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test1.txt")

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()

        val builder = SPALoopTableBuilder(query.graph, transducer.graph, manager);

        val startTime = timeSource.markNow()

//        builder.calculateWithDepthLimit(iterationLimit);
        builder.calculateFullTable();

        val endTime = timeSource.markNow()

        val prewarmTime = startTime - initialTime;
        val iterationTime = endTime - startTime;
        val timePerIteration = iterationTime / iterationLimit;
        val totalTime = endTime - initialTime;

        val estimatedTotalTime = timePerIteration * builder.maxIterationDepth;

        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("Iteration computation time: " + iterationTime)
        println("Time per iteration: " + timePerIteration)
        println("Estimated time to build complete table: " + estimatedTotalTime)

        println("Superclass Cache Size: " + builder.getExecutionContext().dlReasoner.superClassCache.size)
        println("Superclass Cache Hits: " + builder.getExecutionContext().dlReasoner.superClassCacheHitCounter)

        println("Equiv Node Cache Size: " + builder.getExecutionContext().dlReasoner.equivalentClassCache.size)
        println("Superclass Cache Hits: " + builder.getExecutionContext().dlReasoner.equivNodeCacheHitCounter)

        println("SubClasses Cache Size: " + builder.getExecutionContext().dlReasoner.subClassCache.size)
        println("SubClasses Cache Hits: " + builder.getExecutionContext().dlReasoner.subClassCacheHitCounter)

        println("Entailment Check Cache Size: " + builder.getExecutionContext().dlReasoner.entailmentCache.size)
        println("Entailment Cache Hits: " + builder.getExecutionContext().dlReasoner.entailmentCacheHitCounter)
        println("Entailment Cache Misses: " + builder.getExecutionContext().dlReasoner.entailmentCacheMissCounter)


    }

    @Test
    fun benchmarkLoopTableConstruction(){
        val manager = loadExampleOntology();



        val query = readQueryWithFileReaderService("spaCalculation/table/test1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test1.txt")

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic


        val builder = SPALoopTableBuilder(query.graph, transducer.graph, manager);
        val builderWithCache = SPALoopTableBuilder(query.graph, transducer.graph, manager);
        val v2Builder = SPALoopTableBuilder(query.graph, transducer.graph, manager);

        val initialTime = timeSource.markNow()

        builderWithCache.prewarmSubsumptionCahce();


        val startTime = timeSource.markNow()
        builder.calculateFullTable();
        val endTime1 = timeSource.markNow()
        builderWithCache.calculateFullTable();
        val endTime2 = timeSource.markNow();
        v2Builder.calculateFullTableV2();
        val endTime3 = timeSource.markNow();


        val totalTime = endTime2 - initialTime;
        val prewarmTime = startTime - initialTime;
        val time1 = endTime1 - startTime;
        val time2 = endTime2 - endTime1;
        val time3 = endTime3 - endTime2;

        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("Build without prewarmed caches: " + time1)
        println("Build with prewarmed caches: " + time2)
        println("V2 Builder: " + time3)

        println("Superclass Cache Size: " + builder.getExecutionContext().dlReasoner.superClassCache.size)
        println("Superclass Cache Hits: " + builder.getExecutionContext().dlReasoner.superClassCacheHitCounter)

        println("Equiv Node Cache Size: " + builder.getExecutionContext().dlReasoner.equivalentClassCache.size)
        println("Superclass Cache Hits: " + builder.getExecutionContext().dlReasoner.equivNodeCacheHitCounter)

        println("SubClasses Cache Size: " + builder.getExecutionContext().dlReasoner.subClassCache.size)
        println("SubClasses Cache Hits: " + builder.getExecutionContext().dlReasoner.subClassCacheHitCounter)

        println("Entailment Check Cache Size: " + builder.getExecutionContext().dlReasoner.entailmentCache.size)
        println("Entailment Cache Hits: " + builder.getExecutionContext().dlReasoner.entailmentCacheHitCounter)
        println("Entailment Cache Misses: " + builder.getExecutionContext().dlReasoner.entailmentCacheMissCounter)


    }


}