package tin.services.ontology.OntologyExecutionContext

import org.semanticweb.owlapi.model.parameters.Imports
import tin.services.ontology.OntologyManager
import kotlin.math.pow

class OntologyExecutionContext(val manager: OntologyManager) {

    private val alphabet = manager.getAlphabet()
    val conceptNames = alphabet.getConceptNames();
    private val individualNames = alphabet.getIndividualNames();
    private val roleNames = alphabet.getRoleNames();
    private val reasoner = manager.getReasoner();
    val ontology = manager.getOntology();

    var tailsets: HashSet<HashSet<String>>? = null;
    fun prepareForLoopTableConstruction(){
        //create a new instance
        tailsets = computeTailSets();
        //nothing to do for now
    }

    private fun computeTailSets(): HashSet<HashSet<String>>{
        //powerset of all concept names in the ontology
        val classes = ontology.getClassesInSignature(Imports.EXCLUDED);
        val amount = 2.0.pow(classes.size.toDouble()) +1;

        val powerset = powerSet(conceptNames)
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
}