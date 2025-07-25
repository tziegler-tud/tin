package tinCORE.services.ontology

import org.junit.jupiter.api.Test
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinLIB.model.v2.alphabet.Alphabet
import tinCORE.services.internal.fileReaders.*
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService
import tinDL.services.ontology.OntologyManager
import java.io.File


@SpringBootTest
@TestConfiguration
class OntologyManagerTest {

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
    fun testInstanceQueries(){
        val manager = loadExampleOntology();
        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)

        //load parser
        val parser = manager.getQueryParser();
        val ce1 = parser.parseClassExpression("Meal");
        val ce2 = parser.parseClassExpression("Meal AND Vegan");
        val ce3 = parser.parseClassExpression("contains some Pasta");
        val ce4 = parser.parseClassExpression("inverse (serves) some (serves some (Meal AND contains some Pasta)) AND Vegan");
        val a1 = reasoner.getInstances(ce1);
        val a2 = reasoner.getInstances(ce2);
        val a3 = reasoner.getInstances(ce3);
        val a4 = reasoner.getInstances(ce4);

        //get Individuals
        val bruschetta = parser.getNamedIndividual("bruschetta")!!
        val carbonara = parser.getNamedIndividual("carbonara")!!

        val r1 = OWLNamedIndividualNodeSet();
        r1.addEntity(bruschetta)
        r1.addEntity(carbonara)

        val r2 = OWLNamedIndividualNodeSet();
        r2.addEntity(bruschetta)

        val r3 = OWLNamedIndividualNodeSet();
        r3.addEntity(carbonara)

        val r4 = OWLNamedIndividualNodeSet();
        r4.addEntity(bruschetta);

        assert(a1.equals(r1))
        assert(a2.equals(r2))
        assert(a3.equals(r3))
        assert(a4.equals(r4))
    }

    @Test
    fun testClassSubsumption(){
        val manager = loadExampleOntology();

        val reasoner = manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)

        //load parser
        val parser = manager.getQueryParser();
        val ce1 = parser.parseClassExpression("Meal");
        val ce2 = parser.parseClassExpression("contains some (contains some Gluten)");
        val ce3 = parser.parseClassExpression("contains some Pasta AND contains some Egg");
        val a1 = reasoner.getSubClasses(ce1);
        val a2 = reasoner.getSubClasses(ce2);
        val a3 = reasoner.getSubClasses(ce3);

        //get Individuals
        val bruschetta = parser.getOWLClass("Bruschetta")!!
        val carbonara = parser.getOWLClass("Carbonara")!!
        val vegan = parser.getOWLClass("Vegan")!!
        val bread = parser.getOWLClass("Bread")!!
        val pasta = parser.getOWLClass("Pasta")!!

        val nothing = reasoner.bottomClassNode;

        val r1 = OWLClassNodeSet(nothing);
        r1.addEntity(bruschetta)
        r1.addEntity(carbonara)
        r1.addEntity(vegan)

        val r2 = OWLClassNodeSet(nothing);
        r2.addEntity(bread)
        r2.addEntity(pasta)


        val r3 = OWLClassNodeSet(nothing);
        r3.addEntity(carbonara)


        assert(a1.equals(r1))
        assert(a2.equals(r2))
        assert(a3.equals(r3))
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
