package tin.services.ontology.LoopTableBuilder.ELH

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators.SpaS1Calculator
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassRestrictionBuilder
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry
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
    fun testCalculationV2(){
        val manager = loadExampleOntology();

        val query = readQueryWithFileReaderService("spaCalculation/S1/test_spaS1_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S1/test_spaS1_1.txt")

        val ec = manager.createELExecutionContext(ExecutionContextType.ELH);

        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;

        val testRestrictionBuilder = SingleClassRestrictionBuilder(queryParser)



        val s1Calculator = SpaS1Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!
        val M = testRestrictionBuilder.createConceptNameRestriction("Bread")
        val entry = ELSPALoopTableEntry(Pair(s0,t0), Pair(s3,t0),M);

        val entry2 = ELSPALoopTableEntry(Pair(s0,t0), Pair(s2,t0),M);
        val entry3 = ELSPALoopTableEntry(Pair(s1,t0), Pair(s3,t0),M);


        //create empty loop table
        val table: ELSPALoopTable = ELSPALoopTable();
        //fill with non-trivial candidates for testing
        val M1 = testRestrictionBuilder.createConceptNameRestriction("Flour");
        table.set(ELSPALoopTableEntry(Pair(s1,t0),Pair(s2,t0), M1), 2)

        val resultTable = s1Calculator.calculateAll(table, table, true);
        assert(resultTable.get(entry) == 7); // 2 + 2 + 3
        assert(resultTable.get(entry2) == null); // no path found
        assert(resultTable.get(entry3) == null); // no path found
    }

}