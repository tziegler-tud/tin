package tinDL.services.ontology.LoopTableBuilder.ELHI

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.internal.fileReaders.*
import tinDL.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinDL.services.ontology.Reasoner.SimpleDLReasoner
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS2Calculator
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.RestrictionBuilder
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.SPASetLoopTableEntry
import tinDL.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class SpaS2CalculatorTest {
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

    @Test
    fun testCalculation(){
        val skipComparison = true;

        val exampleFile = readWithFileReaderService("pizza3.rdf").get()
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

        val timeSource = TimeSource.Monotonic




        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!


        val M = testRestrictionBuilder.createConceptNameRestriction("Egg")

        //create empty loop table
        val table: ELHISPALoopTable = ELHISPALoopTable();
        val startTime = timeSource.markNow()
        val table2 = s2Calculator.calculateAll(table);
        val endTime = timeSource.markNow()

        val totalTime = endTime - startTime;
        val timePerSet = totalTime.div((ec.tailsetSize/1000UL).toInt())

        println("Tailsets computed: " + ec.tailsetSize)
        println("Total computation time: " + totalTime)
        println("Time per tailsetx1000: " + timePerSet)

        if(!skipComparison) {
            println("Calculating comparison results...")
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

    }
}