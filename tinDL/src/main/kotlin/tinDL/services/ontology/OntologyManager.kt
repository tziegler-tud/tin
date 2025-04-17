package tinDL.services.ontology

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
import tinLIB.model.v1.alphabet.Alphabet
import tinDL.services.ontology.Expressions.DLExpressionBuilder
import tinDL.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHINumericExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyExecutionContext.OntologyExecutionContextFactory
import java.io.File


class OntologyManager(val file: File) {

    private val manager = OWLManager.createOWLOntologyManager();
    private val ontology: OWLOntology = manager.loadOntologyFromOntologyDocument(file);
    private val shortFormProvider: ShortFormProvider = SimpleShortFormProvider()
    val manchesterShortFormProvider: ManchesterOWLSyntaxPrefixNameShortFormProvider = ManchesterOWLSyntaxPrefixNameShortFormProvider(ontology);
    private val executionContextFactory = OntologyExecutionContextFactory();
    private var parser: DLQueryParser = DLQueryParser(ontology, shortFormProvider);

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
//        val vowlJson = Owl2Vowl(ontology).getJsonAsString();
//        //save to json file
//        saveToJson(file.getName(), vowlJson)

        //fill classNames and Iris in one iteration
        classes.forEach{
            if(it.isTopEntity) return@forEach
            classIris.add(it.iri)
            classNames.add(shortFormProvider.getShortForm(it));
        };
        properties.forEach{
            if(it.isTopEntity) return@forEach
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
//                throw IllegalArgumentException("ELK Reasoner is currently not supported.")

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
//        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.SAME_INDIVIDUAL)
        return reasoner;
    }

    fun getQueryParser(): DLQueryParser {
        return parser;
    }

    fun getOntologyInfo(): OntologyInfoData {
        val iri = manager.getOntologyDocumentIRI(ontology);
        val name = iri.shortForm;
        val classCount = classes.size;
        val roleCount = properties.size;
        val individualCount = individuals.size;
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);
        return OntologyInfoData(iri.toString(), name, aboxAxioms, tboxAxioms, signature, classCount, roleCount, individualCount);
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

    fun createExecutionContext(executionContextType: ExecutionContextType, prewarmCaches: Boolean = false) : ExecutionContext {
        return executionContextFactory.create(executionContextType, this, prewarmCaches);
    }

    fun createELHIExecutionContext(executionContextType: ExecutionContextType, prewarmCaches: Boolean = false) : ELHIExecutionContext {
        return executionContextFactory.createELHIContext(executionContextType, this, prewarmCaches);
    }

    fun createELHINumericExecutionContext(prewarmCaches: Boolean = false) : ELHINumericExecutionContext {
        return ELHINumericExecutionContext(this);
    }

    fun createELExecutionContext(executionContextType: ExecutionContextType, prewarmCaches: Boolean = false) : ELExecutionContext {
        return executionContextFactory.createELContext(executionContextType, this, prewarmCaches);
    }
}