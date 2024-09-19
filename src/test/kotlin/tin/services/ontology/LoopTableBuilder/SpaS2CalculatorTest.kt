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
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS2Calculator
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File

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
        val manager = loadExampleOntology();
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S2/test_spaS2_1.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;



        val s2Calculator = SpaS2Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!

        val M = restrictionBuilder.createConceptNameRestriction("Egg")

        val entry = SPALoopTableEntry(Pair(s1,t0), Pair(s2,t0),M);
        val entry2 = SPALoopTableEntry(Pair(s2,t0), Pair(s3,t0),M);
        //create empty loop table
        val table: SPALoopTable = SPALoopTable();

        //calculate spa[(s1,t0),(s2,t0),{Egg}]
        val result = s2Calculator.calculate(entry, table);
        //use s1->Chicken?->s2 with t0-Chicken?|Egg?|2->t0
        assert(result == 2); // 2 + 2 + 3
        table.set(entry, result!!);

        assert(table.get(entry) == result)

        //calculate spa[(s2,t0),(s3,t0),{Egg}]
        val result2 = s2Calculator.calculate(entry2, table);
        //use s2->contains->s3 with t0-contains|Ingredient?|6->t0
        assert(result2 == 6); // 2 + 0 + 3
        table.set(entry2, result2!!);
        assert(table.get(entry2) == result2)

    }

}