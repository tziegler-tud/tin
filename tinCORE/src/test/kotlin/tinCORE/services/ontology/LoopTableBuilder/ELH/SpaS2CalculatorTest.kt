package tinCORE.services.ontology.LoopTableBuilder.ELH

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinCORE.services.internal.fileReaders.*
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService
import tinDL.services.ontology.Reasoner.SimpleDLReasoner
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPALoopTableBuilder
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPLoopTableBuilder
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS2Calculator
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.RestrictionBuilder
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry

import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class SpaS2CalculatorTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false, path: String = "") : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath() + path;
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

    fun loadOntologyWithFilename(filename: String, path: String = ""): OntologyManager {
        val exampleFile = readWithFileReaderService(filename, false, path).get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testCalculationV2(){
        val exampleFile = readWithFileReaderService("pizza_small.rdf").get()
        val manager = OntologyManager(exampleFile);
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")

        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.spaRestrictionBuilder;

        val testRestrictionBuilder = RestrictionBuilder(queryParser, shortFormProvider)




        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!

        val classNames = ec.getClassNames();

        val M = testRestrictionBuilder.createConceptNameRestriction("Egg")

        //create empty loop table
        val table: ELHISPALoopTable = ELHISPALoopTable();
        //calculate spa[(s1,t0),(s2,t0),{Egg}]
        val table2 = s2Calculator.calculateAll(table);
        //use s1->Chicken?->s2 with t0-Chicken?|Egg?|2->t0
        val filtered = table2.getWithRestriction(M);

        ec.forEachTailset {tailset ->
            if(dlReasoner.checkIsSubsumed(expressionBuilder.createELHIExpression(testRestrictionBuilder.asClassExpression(tailset)), expressionBuilder.createELHExpressionFromString("Egg"))) {
                assert(table2.get(ELHISPALoopTableEntry(Pair(s1,t0), Pair(s2,t0),tailset)) == 2)
            }
            else {
                if(dlReasoner.checkIsSubsumed(expressionBuilder.createELHIExpression(testRestrictionBuilder.asClassExpression(tailset)), expressionBuilder.createELHExpressionFromString("Ingredients"))) {
                    assert(table2.get(ELHISPALoopTableEntry(Pair(s0,t0), Pair(s1,t0),tailset)) == 6)
                    assert(table2.get(ELHISPALoopTableEntry(Pair(s2,t0), Pair(s3,t0),tailset)) == 6)
                }
                else {
                    assert(table2.get(ELHISPALoopTableEntry(Pair(s0,t0), Pair(s1,t0), tailset)) == null)
                    assert(table2.get(ELHISPALoopTableEntry(Pair(s1,t0), Pair(s2,t0), tailset)) == null)
                    assert(table2.get(ELHISPALoopTableEntry(Pair(s2,t0), Pair(s3,t0), tailset)) == null)
                }
            }
            assert(table2.get(ELHISPALoopTableEntry(Pair(s0,t0), Pair(s2,t0), tailset)) == null)
            assert(table2.get(ELHISPALoopTableEntry(Pair(s0,t0), Pair(s3,t0), tailset)) == null)
            assert(table2.get(ELHISPALoopTableEntry(Pair(s1,t0), Pair(s3,t0), tailset)) == null)
        }
    }


    @Test
    fun testPaperExample(){
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

        val prewarmTime = startTime - initialTime;
        val spaTime = spaEndTime - startTime;
        val totalTime = spaEndTime - initialTime;


        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("SPA computation time: " + spaTime)

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
        spaTable.map.forEach { (key, value) ->
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

        val motorRes = ec.spaRestrictionBuilder.createConceptNameRestriction("Car");

        assert(spaTable.get(ELSPALoopTableEntry(Pair(s1,t0),Pair(s2,t2),motorRes)) == 0)
        assert(spaTable.get(ELSPALoopTableEntry(Pair(s1,t2),Pair(s2,t2),motorRes)) == 0)

        assert(spaTable.map.size == 2)

    }

}