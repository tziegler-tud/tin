package tin.services.ontology

//import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory
import org.semanticweb.owlapi.util.InferredAxiomGenerator
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator

import org.semanticweb.HermiT.Reasoner as HermitReasoner
import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import org.semanticweb.owlapi.model.parameters.Imports
import java.io.File


class OntologyManager() {

    private val manager = OWLManager.createOWLOntologyManager();
    private lateinit var ontology: OWLOntology;
    private lateinit var reasoner: OWLReasoner;

    enum class BuildInReasoners {
        ELK, JCEL, HERMIT
    }

    fun loadOntology(path: String){

    }

    fun loadOntology(file: File) {
        ontology = manager.loadOntologyFromOntologyDocument(file);
        //extract some ontology insights for testing
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);

        println("Abox axioms: $aboxAxioms");
        println("Tbox axioms: $tboxAxioms");
        println("Signature: $signature");

        return;
    }

    fun loadReasoner(reasonerName: BuildInReasoners, ontology: OWLOntology) {

        val reasonerFactory: OWLReasonerFactory;
        when (reasonerName) {
            BuildInReasoners.ELK -> {
//                reasonerFactory = ElkReasonerFactory();
                //strange dependency problem with google guava 32.2.0
                //TODO: find a fix or throw out ELK support
                return;
            }
            BuildInReasoners.JCEL -> {
                reasonerFactory = JcelReasonerFactory();

            }
            BuildInReasoners.HERMIT -> {
                reasonerFactory = HermitReasoner.ReasonerFactory();

            }
            else -> {
                print("Failed to load reasoner: Unknown reasoner name given.");
                return;
            }
        }
        reasoner = reasonerFactory.createReasoner(ontology)
//
//        // Classify the ontology.
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

        // To generate an inferred ontology we use implementations of
        // inferred axiom generators
//        val gens: MutableList<InferredAxiomGenerator<out OWLAxiom?>> = ArrayList()
//        gens.add(InferredSubClassAxiomGenerator())
//        gens.add(InferredEquivalentClassAxiomGenerator())
    }

    fun getOntologyInfo(): OntologyInfoData {
        val name = manager.getOntologyDocumentIRI(ontology);
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);

        return OntologyInfoData(name.toString(), aboxAxioms, tboxAxioms, signature);
    }
}