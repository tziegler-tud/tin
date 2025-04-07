package tinDL.services.ontology.LoopTableBuilder.ELHI

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.internal.fileReaders.*
import tinDL.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.Reasoner.SimpleDLReasoner
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPLoopTableBuilder
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import tinDL.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class SpLoopTableTest {
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
        val manager = OntologyManager(exampleFile);
        return manager
    }

    fun loadExampleOntologyLarge2() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza_4.rdf").get()
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
        val manager = loadExampleOntologyLarge2();

        val query = readQueryWithFileReaderService("spaCalculation/table/test_comp1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test_comp1.txt")

        val iterationLimit = 2000

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


        val r1 = ec.spaRestrictionBuilder.createConceptNameRestriction("Bruschetta", "Vegan");


        val entry1 = ELHISPALoopTableEntry(Pair(s1,t0), Pair(s0,t0), r1);

        val bruschetta = manager.getQueryParser().getNamedIndividual("bruschetta")!!
        val carbonara = manager.getQueryParser().getNamedIndividual("carbonara")!!

        val bruschettaRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(bruschetta)
        val carbonaraRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(carbonara)

        assert(spTable.get(IndividualLoopTableEntry(Pair(s0,t0),Pair(s1,t0),bruschettaRes)) == 34)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s1,t0),Pair(s0,t0),bruschettaRes)) == 34)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s0,t0),Pair(s2,t0),bruschettaRes)) == 59)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s1,t0),Pair(s2,t0),bruschettaRes)) == 25)

        assert(spTable.get(IndividualLoopTableEntry(Pair(s0,t0),Pair(s1,t0),carbonaraRes)) == 34)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s1,t0),Pair(s0,t0),carbonaraRes)) == 34)

        assert(spTable.map.size == 6)
    }
}