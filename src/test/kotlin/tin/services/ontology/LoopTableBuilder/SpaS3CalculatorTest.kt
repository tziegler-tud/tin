package tin.services.ontology.LoopTableBuilder

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.*
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.Reasoner.SimpleDLReasoner
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS3Calculator
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File

@SpringBootTest
@TestConfiguration
class SpaS3CalculatorTest {
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
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/S3/test_spaS3_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S3/test_spaS3_1.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;



        val s3Calculator = SpaS3Calculator(ec, query.graph, transducer.graph);

        //calculate s3 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!

        val t0 = transducer.graph.getNode("t0")!!
        val t1 = transducer.graph.getNode("t1")!!

        val M = restrictionBuilder.createConceptNameRestriction("Egg")

        val s0t0s0t0 = SPALoopTableEntry(Pair(s0,t0), Pair(s0,t0),M);
        val s0t1s0t1 = SPALoopTableEntry(Pair(s0,t1), Pair(s0,t1),M);
        val s1t0s1t0 = SPALoopTableEntry(Pair(s1,t0), Pair(s1,t0),M);
        val s1t1s1t1 = SPALoopTableEntry(Pair(s1,t1), Pair(s1,t1),M);

        val s0t0s0t1 = SPALoopTableEntry(Pair(s0,t0), Pair(s0,t1),M);
        val s0t0s1t0 = SPALoopTableEntry(Pair(s0,t0), Pair(s1,t0),M);
        val s0t0s1t1 = SPALoopTableEntry(Pair(s0,t0), Pair(s1,t1),M);
        val s0t1s0t0 = SPALoopTableEntry(Pair(s0,t1), Pair(s0,t0),M);

        val s0t1s1t0 = SPALoopTableEntry(Pair(s0,t1), Pair(s1,t0),M);
        val s0t1s1t1 = SPALoopTableEntry(Pair(s0,t1), Pair(s1,t1),M);
        val s1t0s0t0 = SPALoopTableEntry(Pair(s1,t0), Pair(s0,t0),M);
        val s1t0s0t1 = SPALoopTableEntry(Pair(s1,t0), Pair(s0,t1),M);
        val s1t0s1t1 = SPALoopTableEntry(Pair(s1,t0), Pair(s1,t1),M);
        val s1t1s0t0 = SPALoopTableEntry(Pair(s1,t1), Pair(s0,t0),M);
        val s1t1s0t1 = SPALoopTableEntry(Pair(s1,t1), Pair(s0,t1),M);
        val s1t1s1t0 = SPALoopTableEntry(Pair(s1,t1), Pair(s1,t0),M);
        //create empty loop table
        val table: SPALoopTable = SPALoopTable();


        /***********************************************
         *       | s0t0 |  s0t1 |   s1t0 | s1t1
         * ------------------------------------------
         * s0t0 | 0    |  3    |   1    | +inf
         * ------------------------------------------
         * s0t1 | 4     |  0   |   2    | 1
         * ------------------------------------------
         * s1t0 | 5     |  +inf |  0    | 1
         * -------------------------------------------
         * s1t1 |  1    |  0    |  5    | 0
         * ***********************************************/

        table.set(s0t0s0t1, 3)
        table.set(s0t0s1t0, 1)
//        table.set(s0t0s1t1, 0)

        table.set(s0t1s0t0, 4)
        table.set(s0t1s1t0, 2)
        table.set(s0t1s1t1, 1)

        table.set(s1t0s0t0, 5)
//        table.set(s1t0s0t1, 2)
        table.set(s1t0s1t1, 1)

        table.set(s1t1s0t0, 1)
        table.set(s1t1s0t1, 0)
        table.set(s1t1s1t0, 5)

        //calculate spa[(s1,t0),(s2,t0),{Egg}]
        val result = s3Calculator.calculateAll(M, table);

        /****
        Result:
         ****/

        /***********************************************
         *       | s0t0 |  s0t1 |   s1t0 | s1t1
         * ------------------------------------------
         * s0t0 | 0    |  2!    |   1    | 2!
         * ------------------------------------------
         * s0t1 | 2!    |  0    |   2    | 1
         * ------------------------------------------
         * s1t0 | 2!     |  1!   |  0    | 1
         * -------------------------------------------
         * s1t1 |  1    |  0     |  2!   | 0
         * ***********************************************/


        assert(result[s0t0s0t0] == 0)
        assert(result[s0t0s0t1] == 2)
        assert(result[s0t0s1t0] == 1)
        assert(result[s0t0s1t1] == 2)

        assert(result[s0t1s0t0] == 2)
        assert(result[s0t1s0t1] == 0)
        assert(result[s0t1s1t0] == 2)
        assert(result[s0t1s1t1] == 1)

        assert(result[s1t0s0t0] == 2)
        assert(result[s1t0s0t1] == 1)
        assert(result[s1t0s1t0] == 0)
        assert(result[s1t0s1t1] == 1)

        assert(result[s1t1s0t0] == 1)
        assert(result[s1t1s0t1] == 0)
        assert(result[s1t1s1t0] == 2)
        assert(result[s1t1s1t1] == 0)

    }

    @Test
    fun testCalculationV2(){
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = SimpleDLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/S3/test_spaS3_1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/S3/test_spaS3_1.txt")

        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        val queryParser = ec.parser;
        val shortFormProvider = ec.shortFormProvider;
        val restrictionBuilder = ec.restrictionBuilder;



        val s3Calculator = SpaS3Calculator(ec, query.graph, transducer.graph);

        //calculate s3 for a non-trivial entry

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!

        val t0 = transducer.graph.getNode("t0")!!
        val t1 = transducer.graph.getNode("t1")!!

        val M = restrictionBuilder.createConceptNameRestriction("Egg")

        val s0t0s0t0 = SPALoopTableEntry(Pair(s0,t0), Pair(s0,t0),M);
        val s0t1s0t1 = SPALoopTableEntry(Pair(s0,t1), Pair(s0,t1),M);
        val s1t0s1t0 = SPALoopTableEntry(Pair(s1,t0), Pair(s1,t0),M);
        val s1t1s1t1 = SPALoopTableEntry(Pair(s1,t1), Pair(s1,t1),M);

        val s0t0s0t1 = SPALoopTableEntry(Pair(s0,t0), Pair(s0,t1),M);
        val s0t0s1t0 = SPALoopTableEntry(Pair(s0,t0), Pair(s1,t0),M);
        val s0t0s1t1 = SPALoopTableEntry(Pair(s0,t0), Pair(s1,t1),M);
        val s0t1s0t0 = SPALoopTableEntry(Pair(s0,t1), Pair(s0,t0),M);

        val s0t1s1t0 = SPALoopTableEntry(Pair(s0,t1), Pair(s1,t0),M);
        val s0t1s1t1 = SPALoopTableEntry(Pair(s0,t1), Pair(s1,t1),M);
        val s1t0s0t0 = SPALoopTableEntry(Pair(s1,t0), Pair(s0,t0),M);
        val s1t0s0t1 = SPALoopTableEntry(Pair(s1,t0), Pair(s0,t1),M);
        val s1t0s1t1 = SPALoopTableEntry(Pair(s1,t0), Pair(s1,t1),M);
        val s1t1s0t0 = SPALoopTableEntry(Pair(s1,t1), Pair(s0,t0),M);
        val s1t1s0t1 = SPALoopTableEntry(Pair(s1,t1), Pair(s0,t1),M);
        val s1t1s1t0 = SPALoopTableEntry(Pair(s1,t1), Pair(s1,t0),M);
        //create empty loop table
        val table: SPALoopTable = SPALoopTable();


        /***********************************************
         *       | s0t0 |  s0t1 |   s1t0 | s1t1
         * ------------------------------------------
         * s0t0 | 0    |  3    |   1    | +inf
         * ------------------------------------------
         * s0t1 | 4     |  0   |   2    | 1
         * ------------------------------------------
         * s1t0 | 5     |  +inf |  0    | 1
         * -------------------------------------------
         * s1t1 |  1    |  0    |  5    | 0
         * ***********************************************/

        table.set(s0t0s0t1, 3)
        table.set(s0t0s1t0, 1)
//        table.set(s0t0s1t1, 0)

        table.set(s0t1s0t0, 4)
        table.set(s0t1s1t0, 2)
        table.set(s0t1s1t1, 1)

        table.set(s1t0s0t0, 5)
//        table.set(s1t0s0t1, 2)
        table.set(s1t0s1t1, 1)

        table.set(s1t1s0t0, 1)
        table.set(s1t1s0t1, 0)
        table.set(s1t1s1t0, 5)

        //calculate spa[(s1,t0),(s2,t0),{Egg}]
        val result = s3Calculator.calculateAllV2(table);

        /****
        Result:
         ****/

        /***********************************************
         *       | s0t0 |  s0t1 |   s1t0 | s1t1
         * ------------------------------------------
         * s0t0 | 0    |  2!    |   1    | 2!
         * ------------------------------------------
         * s0t1 | 2!    |  0    |   2    | 1
         * ------------------------------------------
         * s1t0 | 2!     |  1!   |  0    | 1
         * -------------------------------------------
         * s1t1 |  1    |  0     |  2!   | 0
         * ***********************************************/


        assert(result[s0t0s0t0] == 0)
        assert(result[s0t0s0t1] == 2)
        assert(result[s0t0s1t0] == 1)
        assert(result[s0t0s1t1] == 2)

        assert(result[s0t1s0t0] == 2)
        assert(result[s0t1s0t1] == 0)
        assert(result[s0t1s1t0] == 2)
        assert(result[s0t1s1t1] == 1)

        assert(result[s1t0s0t0] == 2)
        assert(result[s1t0s0t1] == 1)
        assert(result[s1t0s1t0] == 0)
        assert(result[s1t0s1t1] == 1)

        assert(result[s1t1s0t0] == 1)
        assert(result[s1t1s0t1] == 0)
        assert(result[s1t1s1t0] == 2)
        assert(result[s1t1s1t1] == 0)

    }

}