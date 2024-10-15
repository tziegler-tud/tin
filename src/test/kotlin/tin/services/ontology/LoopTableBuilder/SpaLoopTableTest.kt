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
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTableBuilder.SPALoopTableBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File

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
        val exampleFile = readWithFileReaderService("pizza2_test.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testLoopTableInitialStep() {
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("test1.txt")
        val transducer = readTransducerWithFileReaderService("test1.txt")

        val builder = SPALoopTableBuilder(query.graph, transducer.graph, manager);
        builder.calculateInitialStep();
        assert(true)
    }

    @Test
    fun testLoopTableConstruction(){
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);

        val query = readQueryWithFileReaderService("spaCalculation/table/test1.txt")
        val transducer = readTransducerWithFileReaderService("spaCalculation/table/test1.txt")

        val builder = SPALoopTableBuilder(query.graph, transducer.graph, manager);
        builder.calculateFullTable();
        println("Superclass Cache Size: " + builder.getExecutionContext().dlReasoner.superClassCache.size)
        println("Superclass Cache Hits: " + builder.getExecutionContext().dlReasoner.superClassCacheHitCounter)

        println("Equiv Node Cache Size: " + builder.getExecutionContext().dlReasoner.equivalentClassCache.size)
        println("Superclass Cache Hits: " + builder.getExecutionContext().dlReasoner.equivNodeCacheHitCounter)

        println("SubClasses Cache Size: " + builder.getExecutionContext().dlReasoner.subClassCache.size)
        println("SubClasses Cache Hits: " + builder.getExecutionContext().dlReasoner.subClassCacheHitCounter)

        println("Entailment Check Cache Size: " + builder.getExecutionContext().dlReasoner.entailmentCache.size)
        println("Entailment Cache Hits: " + builder.getExecutionContext().dlReasoner.entailmentCacheHitCounter)
        println("Entailment Cache Misses: " + builder.getExecutionContext().dlReasoner.entailmentCacheMissCounter)


    }


}