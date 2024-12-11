package tin.services.ontology.LoopTableBuilder.ELH

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.internal.utils.DLTransducerFactory
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPLoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class SpLoopTableTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false, path: String = "") : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath() + path;
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readQueryWithFileReaderService(fileName: String, breakOnError: Boolean = false, path: String = "") : FileReaderResult<QueryGraph> {
        var fileReaderService: QueryReaderServiceV2 = QueryReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getQueryPath() + path;
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readTransducerWithFileReaderService(fileName: String, breakOnError: Boolean = false, path: String = "") : FileReaderResult<TransducerGraph> {
        var fileReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getTransducerPath() + path;
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    fun loadOntologyWithFilename(filename: String, path: String = ""): OntologyManager {
        val exampleFile = readWithFileReaderService(filename, false, path).get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    fun loadExampleOntology() : OntologyManager {
        return loadOntologyWithFilename("pizza2.rdf")
    }

    fun loadExampleOntologyLarge() : OntologyManager {
        return loadOntologyWithFilename("pizza3_1.rdf")

    }

    fun loadExampleOntologyLarge2() : OntologyManager {
        return loadOntologyWithFilename("pizza_4.rdf")
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

//    @Test
//    fun multiTestRuner(){
//        runPaperExample();
//    }

    @Test
    fun testLoopTableConstruction(){
        val manager = loadExampleOntologyLarge();

        val query = readQueryWithFileReaderService("spaCalculation/table/test_comp1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test_comp1.txt")

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH, false);
//        ec.prewarmSubsumptionCache()
        val builder = ELSPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        val spBuilder = ELSPLoopTableBuilder(query.graph, transducer.graph, manager, ec);

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

        //debugging expressions:

        //(s1,t0),(s0,t0), Bruschetta,Vegan
        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val t0 = transducer.graph.getNode("t0")!!

    }

    @Test
    fun testPaperExample(){
        runPaperExample()
    }

    fun runPaperExample(){
        val manager = loadOntologyWithFilename("ELH/test_paper1.rdf");

        val query = readQueryWithFileReaderService("test_paper1.txt")
        val transducer = readTransducerWithFileReaderService("test_paper1.txt")

        val iterationLimit = 2000

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH, false);
//        ec.prewarmSubsumptionCache()
        val builder = ELSPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        val spBuilder = ELSPLoopTableBuilder(query.graph, transducer.graph, manager, ec);

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

        //debugging expressions:

        //(s1,t0),(s0,t0), Bruschetta,Vegan
        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val t0 = transducer.graph.getNode("t0")!!
        val t1 = transducer.graph.getNode("t1")!!
        val t2 = transducer.graph.getNode("t2")!!

        val carRes = ec.spaRestrictionBuilder.createConceptNameRestriction("Car");

        val a = manager.getQueryParser().getNamedIndividual("a")!!
        val b = manager.getQueryParser().getNamedIndividual("b")!!
        val g = manager.getQueryParser().getNamedIndividual("g")!!


        assert(spTable.get(ELSPLoopTableEntry(Pair(s0,t2),Pair(s2,t2),carRes)) == 0)
        assert(spTable.get(ELSPLoopTableEntry(Pair(s0,t0),Pair(s2,t2),carRes)) == 0)

        assert(spTable.map.size == 2)

    }


    @Test
    fun runLUBMExample(){
        val manager = loadOntologyWithFilename("LUBM/merged/merge_0_0.rdf");
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

    }
}