package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface MultiClassLoopTableEntryRestriction : LoopTableEntryRestriction {


    fun asSet(): Set<OWLClass>;

    fun asList(): List<OWLClass>;

    fun containsElement(element: OWLClass): Boolean;

    fun containsElementFromSet(set: Set<OWLClass>) : Boolean

    fun containsAllElementsFromSet(set: Set<OWLClass>) : Boolean

    fun containsAllElementsFromOneOf(set: Set<MultiClassLoopTableEntryRestriction>) : Boolean

    /**
     * returns true if this is a subset of the given restriction
     */
    fun isSubsetOf(restriction: MultiClassLoopTableEntryRestriction) : Boolean

    /**
     * returns true if this is a superset of the given restriction
     */
    fun isSupersetOf(restriction: MultiClassLoopTableEntryRestriction) : Boolean

    fun addElement(element: OWLClass): Boolean
}