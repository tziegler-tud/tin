package tin.services.ontology

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import org.semanticweb.elk.owlapi.ElkReasoner
import org.semanticweb.elk.owlapi.ElkReasonerFactory

import de.uni_stuttgart.vis.vowl.owl2vowl.Owl2Vowl
import org.semanticweb.owlapi.expression.OWLExpressionParser
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser
import org.semanticweb.HermiT.Reasoner as HermitReasoner
import org.semanticweb.HermiT.ReasonerFactory as HermitReasonerFactory
//import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.util.ShortFormProvider
import org.semanticweb.owlapi.util.SimpleShortFormProvider
import tin.model.alphabet.Alphabet
import java.io.File


class OntologyManager(val file: File) {

    private val manager = OWLManager.createOWLOntologyManager();
    private val ontology: OWLOntology = manager.loadOntologyFromOntologyDocument(file);
    private val shortFormProvider: ShortFormProvider = SimpleShortFormProvider()
    private var parser: DLQueryParser = DLQueryParser(ontology, shortFormProvider);

    private lateinit var reasoner: OWLReasoner;
    private var currentReasoner = BuildInReasoners.NONE;

    enum class BuildInReasoners {
        NONE, ELK, JCEL, HERMIT
    }

    init {
        //create json representation for vowl
        val vowlJson = Owl2Vowl(ontology).getJsonAsString();
        //save to json file
        saveToJson(file.getName(), vowlJson)
    }

    fun getOntology(): OWLOntology {
        return ontology;
    }

    fun getShortFormProvider(): ShortFormProvider{
        return shortFormProvider
    }

    fun loadReasoner(reasonerName: BuildInReasoners): OWLReasoner {

        val reasonerFactory: OWLReasonerFactory;
        when (reasonerName) {
            BuildInReasoners.ELK -> {
//                reasonerFactory = ElkReasonerFactory();
                //strange dependency problem with google guava 32.2.0
                //TODO: find a fix or throw out ELK support
                currentReasoner = reasonerName;
                reasonerFactory = ElkReasonerFactory();
                throw IllegalArgumentException("ELK Reasoner is currently not supported.")

            }
            BuildInReasoners.JCEL -> {
                //JCEL currently does not support OWLAPI 5.5, disable for now
                currentReasoner = BuildInReasoners.NONE;
//                reasonerFactory = JcelReasonerFactory();
                throw IllegalArgumentException("JCEL Reasoner is currently not supported.")
            }
            BuildInReasoners.HERMIT -> {
                currentReasoner = reasonerName;
                reasonerFactory = HermitReasonerFactory();

            }
            else -> {
                currentReasoner = BuildInReasoners.NONE;
                throw IllegalArgumentException("Failed to load reasoner: Unknown reasoner name given.");
            }
        }
        reasoner = reasonerFactory.createReasoner(ontology)
        // Classify the ontology.
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
        return reasoner;
    }

    fun getReasoner(): OWLReasoner? {
        if(this::reasoner.isInitialized){
            return reasoner;
        }
        else return null;
    }

    fun getQueryParser(): DLQueryParser {
        return parser;
    }

    fun getOntologyInfo(): OntologyInfoData {
        val name = manager.getOntologyDocumentIRI(ontology);
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);
        val reasoner = currentReasoner;

        return OntologyInfoData(name.toString(), aboxAxioms, tboxAxioms, signature, reasoner);
    }

    fun getAlphabet(): Alphabet {
        val classes = ontology.getClassesInSignature(Imports.EXCLUDED);
        val properties = ontology.getObjectPropertiesInSignature(Imports.EXCLUDED);
        val individuals = ontology.getIndividualsInSignature(Imports.EXCLUDED);

        val alphabet = Alphabet();
        //do not include top objects owl:Thing (the top class), owl:topObjectProperty (the top object property) , owl:topDataProperty
        classes.forEach { if(!it.isTopEntity) alphabet.addConceptName(shortFormProvider.getShortForm(it)) }
        properties.forEach { if(!it.isTopEntity) alphabet.addRoleName(shortFormProvider.getShortForm(it)) }
        individuals.forEach { if(!it.isTopEntity) alphabet.addIndividualName(shortFormProvider.getShortForm(it)) }
        return alphabet;
    }

    private fun saveToJson(filename: String, jsonString: String){
        val f = File("src/main/resources/vowl/$filename.json")
        f.createNewFile()
        f.writeText(jsonString)
    }
}