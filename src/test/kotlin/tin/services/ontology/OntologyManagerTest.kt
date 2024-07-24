package tin.services.ontology

import org.junit.jupiter.api.Test
import org.semanticweb.owlapi.reasoner.InferenceType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration

import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.technical.SystemConfigurationService

import java.io.File


@SpringBootTest
@TestConfiguration
class OntologyManagerTest {

    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    private fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun loadExampleOntology() : OntologyManager {
        val exampleFile = readWithFileReaderService("pizza.rdf").get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    @Test
    fun employHermitReasoner(){
        val manager = loadExampleOntology();
        manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)

        //use reasoner to answer simple subsumption

    }

    @Test
    fun getAlphabet() {

    }
}