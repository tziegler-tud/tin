package tin.services.ontology.OntologyExecutionContext

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.util.ShortFormProvider
import org.semanticweb.owlapi.util.SimpleShortFormProvider
import tin.services.ontology.DLQueryParser
import tin.services.ontology.OntologyManager
import kotlin.math.pow

class OntologyExecutionContext(private val manager: OntologyManager) {

    private var ontology = manager.getOntology();
    private var classes = manager.classes;

    var tailsets: HashSet<HashSet<String>>? = null;
    fun prepareForLoopTableConstruction(){
        //create a new instance
        tailsets = computeTailSets();
        //nothing to do for now
    }

    private fun computeTailSets(): HashSet<HashSet<String>>{
        //powerset of all concept names in the ontology
        val classes = manager.getClassNames();
        val amount = 2.0.pow(classes.size.toDouble()) +1;

        val powerset = powerSet<String>(classes)
        return powerset;
    }

    private fun <T> powerSet(originalSet: HashSet<T>): HashSet<HashSet<T>> {
        // Start with the empty set
        val powerSet = HashSet<HashSet<T>>()
        powerSet.add(HashSet())

        // Iterate over each element in the original set
        for (element in originalSet) {
            val newSubsets = HashSet<HashSet<T>>()

            // For each subset in the current powerSet, add the current element
            for (subset in powerSet) {
                val newSubset = HashSet(subset)
                newSubset.add(element)
                newSubsets.add(newSubset)
            }

            // Add the newly created subsets to the powerset
            powerSet.addAll(newSubsets)
        }
        return powerSet
    }

    fun getClasses(): Set<OWLEntity> {
        return classes;
    }

    fun getClassAmount(): Int {
        return classes.size;
    }

    fun getClassNames(): HashSet<String> {
        return manager.getClassNames();
    }
}