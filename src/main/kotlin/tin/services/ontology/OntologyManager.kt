package tin.services.ontology

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import org.semanticweb.elk.owlapi.ElkReasonerFactory

import de.uni_stuttgart.vis.vowl.owl2vowl.Owl2Vowl
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider
import org.semanticweb.owlapi.model.*
import org.semanticweb.HermiT.ReasonerFactory as HermitReasonerFactory
//import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.util.ShortFormProvider
import org.semanticweb.owlapi.util.SimpleShortFormProvider
import tin.model.v1.alphabet.Alphabet
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContextFactory
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import java.io.File


class OntologyManager(val file: File) {

    private val manager = OWLManager.createOWLOntologyManager();
    private val ontology: OWLOntology = manager.loadOntologyFromOntologyDocument(file);
    private val shortFormProvider: ShortFormProvider = SimpleShortFormProvider()
    val manchesterShortFormProvider: ShortFormProvider = ManchesterOWLSyntaxPrefixNameShortFormProvider(ontology);
    private val executionContextFactory = OntologyExecutionContextFactory();
    private var parser: DLQueryParser = DLQueryParser(ontology, shortFormProvider);
    private var restrictionBuilder = RestrictionBuilder(parser, shortFormProvider);

    private val expressionBuilder = DLExpressionBuilder(this);

    private val name = manager.getOntologyDocumentIRI(ontology);
    private val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
    private val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
    private val signature = ontology.getSignature(Imports.EXCLUDED);

    val classes: Set<OWLClass> = ontology.getClassesInSignature(Imports.EXCLUDED);
    val properties: Set<OWLObjectProperty> = ontology.getObjectPropertiesInSignature(Imports.EXCLUDED);
    val individuals: Set<OWLNamedIndividual> = ontology.getIndividualsInSignature(Imports.EXCLUDED);

    private val classNames: HashSet<String> = HashSet();
    private val roleNames: HashSet<String> = HashSet();
    private val classIris: HashSet<IRI> = HashSet();

    enum class BuildInReasoners {
        NONE, ELK, JCEL, HERMIT
    }

    init {
        //create json representation for vowl
        val vowlJson = Owl2Vowl(ontology).getJsonAsString();
        //save to json file
        saveToJson(file.getName(), vowlJson)

        //fill classNames and Iris in one iteration
        classes.forEach{
            classIris.add(it.iri)
            classNames.add(shortFormProvider.getShortForm(it));
        };
        properties.forEach{
            roleNames.add(shortFormProvider.getShortForm(it))
        }
    }

    fun getOntology(): OWLOntology {
        return ontology;
    }

    fun getShortFormProvider(): ShortFormProvider{
        return shortFormProvider
    }

    fun getExpressionBuilder(): DLExpressionBuilder {
        return expressionBuilder;
    }

    fun createReasoner(reasonerName: BuildInReasoners): OWLReasoner {
        val reasonerFactory: OWLReasonerFactory;
        when (reasonerName) {
            BuildInReasoners.ELK -> {
//                reasonerFactory = ElkReasonerFactory();
                //strange dependency problem with google guava 32.2.0
                //TODO: find a fix or throw out ELK support
                reasonerFactory = ElkReasonerFactory();
                throw IllegalArgumentException("ELK Reasoner is currently not supported.")

            }
            BuildInReasoners.JCEL -> {
                //JCEL currently does not support OWLAPI 5.5, disable for now
//                reasonerFactory = JcelReasonerFactory();
                throw IllegalArgumentException("JCEL Reasoner is currently not supported.")
            }
            BuildInReasoners.HERMIT -> {
                reasonerFactory = HermitReasonerFactory();

            }
            else -> {
                throw IllegalArgumentException("Failed to load reasoner: Unknown reasoner name given.");
            }
        }
        val reasoner = reasonerFactory.createReasoner(ontology)
        // Classify the ontology.
        val precomputableInferenceTypes = reasoner.precomputableInferenceTypes;
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.SAME_INDIVIDUAL)
        return reasoner;
    }

    fun getQueryParser(): DLQueryParser {
        return parser;
    }

    fun getRestrictionBuilder() : RestrictionBuilder{
        return restrictionBuilder;
    }

    fun getOntologyInfo(): OntologyInfoData {
        val name = manager.getOntologyDocumentIRI(ontology);
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);
        return OntologyInfoData(name.toString(), aboxAxioms, tboxAxioms, signature);
    }


    fun getAlphabet(): Alphabet {
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

    fun getClassNames(): HashSet<String>{
        return classNames;
    }

    fun getRoleNames(): HashSet<String> {
        return roleNames;
    }

    fun getRoles(): Set<OWLObjectProperty> {
        return properties;
    }

    fun getClassIris(): HashSet<IRI> {
        return classIris;
    }

    fun createExecutionContext(executionContextType: ExecutionContextType) : OntologyExecutionContext {
        return executionContextFactory.create(executionContextType, this);
    }
}