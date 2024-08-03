package tin.services.ontology

import org.junit.jupiter.api.Test
import org.semanticweb.owlapi.reasoner.InferenceType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.alphabet.Alphabet

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
        manager.getOntologyInfo();

        //use reasoner to answer simple subsumption

    }

    @Test
    fun testAlphabet() {
        val manager = loadExampleOntology();
        val alphabet = manager.getAlphabet();

        val a = Alphabet();
        val conceptNames = hashSetOf(
            "Restaurant",
            "Meal",
            "Bruschetta",
            "Carbonara",
            "Bread",
            "Pasta",
            "Flour",
            "Chicken",
            "Egg",
            "Gluten",
            "Ingredients",
            "Vegan"
            )
        val roleNames = hashSetOf(
            "contains",
            "serves",
            "is_contained"
        )

        val individualNames = hashSetOf(
            "r",
            "bruschetta",
            "carbonara"
        )

        a.addConceptNames(conceptNames)
        a.addRoleNames(roleNames);
        a.addIndividualNames(individualNames);

        assert(a == alphabet);
    }
}