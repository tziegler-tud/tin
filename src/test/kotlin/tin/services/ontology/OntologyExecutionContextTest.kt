package tin.services.ontology

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser
import org.junit.jupiter.api.Test
import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.expression.ShortFormEntityChecker
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter
import org.semanticweb.owlapi.util.ShortFormProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.alphabet.Alphabet
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.technical.SystemConfigurationService
import java.io.File


@SpringBootTest
@TestConfiguration
class OntologyExecutionContextTest {
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
    fun testPowersetConstruction(){
        val manager = loadExampleOntology();
        val ec = manager.createExecutionContext(ExecutionContextType.LOOPTABLE);
        ec.prepareForLoopTableConstruction();
        val classAmount = ec.getClassAmount();
        val classNames = ec.getClassNames();

        val powerset = ec.tailsets!!;
        assert(powerset.size == Math.pow(2.0, classAmount.toDouble()).toInt())

        val e1 = classNames.first();
        val e2 = classNames.elementAt(1);
        val e3 = classNames.elementAt(2);
        val e4 = classNames.elementAt(3);
        val e5 = classNames.elementAt(4);
        val e6 = classNames.elementAt(5);

        val testset_1el_1: HashSet<String> = hashSetOf(e1);
        val testset_1el_2: HashSet<String> = hashSetOf(e2);
        val testset_1el_3: HashSet<String> = hashSetOf(e3);
        val testset_1el_4: HashSet<String> = hashSetOf(e4);
        val testset_2el_1: HashSet<String> = hashSetOf(e1, e2)
        val testset_2el_2: HashSet<String> = hashSetOf(e1, e3)
        val testset_3el_1: HashSet<String> = hashSetOf(e1, e2, e3)
        val testset_3el_2: HashSet<String> = hashSetOf(e1, e3, e5)
        val testset_4el_1: HashSet<String> = hashSetOf(e1, e2, e3, e4)
        val testset_4el_2: HashSet<String> = hashSetOf(e1, e3, e5, e6)
        assert(powerset.contains(testset_1el_1));
        assert(powerset.contains(testset_1el_2));
        assert(powerset.contains(testset_1el_3));
        assert(powerset.contains(testset_1el_4));
        assert(powerset.contains(testset_2el_1));
        assert(powerset.contains(testset_2el_2));
        assert(powerset.contains(testset_2el_2));
        assert(powerset.contains(testset_3el_1));
        assert(powerset.contains(testset_3el_2));
        assert(powerset.contains(testset_4el_1));
        assert(powerset.contains(testset_4el_2));
    }
}