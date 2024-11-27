package tin.services.ontology.LoopTableBuilder.ELHI

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
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

    fun loadExampleOntologyLarge() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza3.rdf").get()
//        val exampleFile = readWithFileReaderService("univ-bench.owl.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }
    fun loadExampleOntologyLarge2() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza_4.rdf").get()
//        val exampleFile = readWithFileReaderService("univ-bench.owl.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    fun loadExampleOntologyUnivbench() : OntologyManager {
        val exampleFile = readWithFileReaderService("univ-bench.owl.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testLoopTableInitialStep() {
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("test1.txt")
        val transducer = readTransducerWithFileReaderService("test1.txt")


        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI, false);
        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        builder.calculateInitialStep();
        assert(true)
    }

    @Test
    fun testLoopTableConstruction(){
        val manager = loadExampleOntologyLarge();



        val query = readQueryWithFileReaderService("spaCalculation/table/test1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test1.txt")

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
//        ec.prewarmSubsumptionCache()
        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);

        val startTime = timeSource.markNow()

//        builder.calculateWithDepthLimit(iterationLimit);
        builder.calculateFullTable();

        val endTime = timeSource.markNow()

        val prewarmTime = startTime - initialTime;
        val iterationTime = endTime - startTime;
        val timePerIteration = iterationTime / iterationLimit;
        val totalTime = endTime - initialTime;

//        val estimatedTotalTime = timePerIteration * builder.maxIterationDepth;

        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("Iteration computation time: " + iterationTime)
        println("Time per iteration: " + timePerIteration)
//        println("Estimated time to build complete table: " + estimatedTotalTime)

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


    }

    @Test
    fun testLoopTableConstructionUnivBench(){
        val manager = loadExampleOntologyUnivbench()



        val query = readQueryWithFileReaderService("spaCalculation/table/univbench-test1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/univbench-test1.txt")

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
//        ec.prewarmSubsumptionCache()
        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);

        val startTime = timeSource.markNow()

//        builder.calculateWithDepthLimit(iterationLimit);
        builder.calculateFullTable();

        val endTime = timeSource.markNow()

        val prewarmTime = startTime - initialTime;
        val iterationTime = endTime - startTime;
        val timePerIteration = iterationTime / iterationLimit;
        val totalTime = endTime - initialTime;

//        val estimatedTotalTime = timePerIteration * builder.maxIterationDepth;

        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("Iteration computation time: " + iterationTime)
        println("Time per iteration: " + timePerIteration)
//        println("Estimated time to build complete table: " + estimatedTotalTime)

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


    }

    @Test
    fun benchmarkLoopTableConstruction(){
        val manager = loadExampleOntology();



        val query = readQueryWithFileReaderService("spaCalculation/table/test1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test1.txt")

        val timeSource = TimeSource.Monotonic

        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI, false);
        val ecNumeric = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);



        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        val builderWithCache = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);

        val numericBuilder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ecNumeric);
        val numericBuilderWithCache = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ecNumeric);

        val initialTime = timeSource.markNow()

        builderWithCache.prewarmSubsumptionCache();
        val prewarm1 = timeSource.markNow()
        numericBuilderWithCache.prewarmSubsumptionCache();
        val prewarm2 = timeSource.markNow()



        val startTime = timeSource.markNow()
        builder.calculateFullTable();
        val endTime1 = timeSource.markNow()
        builderWithCache.calculateFullTable();
        val endTime2 = timeSource.markNow();
        numericBuilder.calculateFullTable();
        val endTime3 = timeSource.markNow();
        numericBuilderWithCache.calculateFullTable();
        val endTime4 = timeSource.markNow();


        val totalTime = endTime2 - initialTime;
        val prewarmTime1 = prewarm1 - initialTime;
        val prewarmTime2 = prewarm2 - prewarm1;
        val time1 = endTime1 - startTime;
        val time2 = endTime2 - endTime1;
        val time3 = endTime3 - endTime2;
        val time4 = endTime4 - endTime3;

        println("Total computation time: " + totalTime)
        println("Cache prewarming (set builder): " + prewarmTime1)
        println("Cache prewarming (numeric builder): " + prewarmTime2)
        println("Set Build without prewarmed caches: " + time1)
        println("Set Build with prewarmed caches: " + time2)
        println("Numeric Build with prewarmed caches: " + time3)
        println("Numeric Build with prewarmed caches: " + time4)

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


    }


}