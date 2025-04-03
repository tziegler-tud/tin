package tinDL.services.ontology.LoopTableBuilder.ELHI

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinDL.model.v2.query.QueryGraph
import tinDL.model.v2.transducer.TransducerGraph
import tinDL.services.internal.fileReaders.*
import tinDL.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS1Calculator
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.RestrictionBuilder
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tinDL.services.technical.SystemConfigurationService
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

        val query = readQueryWithFileReaderService("spaCalculation/S1/test_spaS1_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S1/test_spaS1_1.txt")

        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI);

        val dlReasoner = ec.dlReasoner
        val expressionBuilder = ec.expressionBuilder
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.spaRestrictionBuilder;

        val testRestrictionBuilder = RestrictionBuilder(queryParser, shortFormProvider)



        val s1Calculator = SpaS1Calculator(ec, query.graph, transducer.graph);

        //calculate s1 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val s3 = query.graph.getNode("s3")!!

        val t0 = transducer.graph.getNode("t0")!!
        val M = testRestrictionBuilder.createConceptNameRestriction("Bread")
        val entry = ELHISPALoopTableEntry(Pair(s0,t0), Pair(s3,t0),M);

        val entry2 = ELHISPALoopTableEntry(Pair(s0,t0), Pair(s2,t0),M);
        val entry3 = ELHISPALoopTableEntry(Pair(s1,t0), Pair(s3,t0),M);


        //create empty loop table
        val table: ELHISPALoopTable = ELHISPALoopTable();
        //fill with non-trivial candidates for testing
        val M1 = testRestrictionBuilder.createConceptNameRestriction("Flour");
        table.set(ELHISPALoopTableEntry(Pair(s1,t0),Pair(s2,t0), M1), 2)

        val resultTable = s1Calculator.calculateAll(table, table, true);
        assert(resultTable.get(entry) == 7); // 2 + 2 + 3
        assert(resultTable.get(entry2) == null); // no path found
        assert(resultTable.get(entry3) == null); // no path found
    }

}