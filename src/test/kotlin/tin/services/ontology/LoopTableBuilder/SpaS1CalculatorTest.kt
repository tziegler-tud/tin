package tin.services.ontology.LoopTableBuilder

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.DLReasoner
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.SPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS1Calculator
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File

@SpringBootTest
@TestConfiguration
class SpaS1CalculatorTest {
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

        val query = readQueryWithFileReaderService("test2.txt")
        val transducer = readTransducerWithFileReaderService("test2.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;



        val s1Calculator = SpaS1Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!
        val M = restrictionBuilder.createConceptNameRestriction("Bread")
        val entry = SPALoopTableEntry(Pair(s0,t0), Pair(s3,t0),M);


        //create empty loop table
        val table: SPALoopTable = SPALoopTable();
        //fill with non-trivial candidates for testing
        val M1 = restrictionBuilder.createConceptNameRestriction("Flour");
        table.set(SPALoopTableEntry(Pair(s1,t0),Pair(s2,t0), M1), 2)


        val result = s1Calculator.calculate(entry, table);

    }

}