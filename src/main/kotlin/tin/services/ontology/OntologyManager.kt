package tin.services.ontology

import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory
import org.semanticweb.owlapi.util.InferredAxiomGenerator
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator
import java.io.File


class OntologyManager() {

    private val manager = OWLManager.createOWLOntologyManager();
    private lateinit var ontology: OWLOntology;
    private lateinit var reasoner: OWLReasoner;

    fun loadOntology(file: File){
        val ontology = manager.loadOntologyFromOntologyDocument(file)

    }

    fun loadElkReasoner(ontology: OWLOntology){
        // We need a reasoner to do our query answering
        val reasonerFactory: OWLReasonerFactory = ElkReasonerFactory()
        val reasoner = reasonerFactory.createReasoner(ontology)

        // Classify the ontology.
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

        // To generate an inferred ontology we use implementations of
        // inferred axiom generators
//        val gens: MutableList<InferredAxiomGenerator<out OWLAxiom?>> = ArrayList()
//        gens.add(InferredSubClassAxiomGenerator())
//        gens.add(InferredEquivalentClassAxiomGenerator())
        reasoner.dispose();
    }
}