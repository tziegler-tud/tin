package tin.services.ontology

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.math.exp

@SpringBootTest
@TestConfiguration
class DLReasonerTest {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    fun loadExampleOntology() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza2.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun testSubsumptionCheck(){
        val manager = loadExampleOntology();
        val reasoner = manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val expressionBuilder = manager.getExpressionBuilder();
        val dlReasoner = DLReasoner(reasoner, expressionBuilder);


        val l1 = expressionBuilder.createELHExpressionFromString("Bread")
        val l2 = expressionBuilder.createELHExpressionFromString("Pasta")
        val r1 = expressionBuilder.createELHExpressionFromString("contains some Flour");
        val r2 = expressionBuilder.createELHExpressionFromString("contains some Chicken");

        assert(dlReasoner.checkIsSubsumed(l1, r1));
        assert(dlReasoner.checkIsSubsumed(l2, r1));
        assert(!dlReasoner.checkIsSubsumed(l1, r2));
        assert(!dlReasoner.checkIsSubsumed(l2, r2));
    }


}