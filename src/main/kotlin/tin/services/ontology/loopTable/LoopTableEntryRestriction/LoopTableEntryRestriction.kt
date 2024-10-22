package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.model.OWLClass

interface LoopTableEntryRestriction<DataType> {
    val value: Any

    fun isEmpty(): Boolean;

    fun asSet(): Set<DataType>;

    fun asList(): List<DataType>;

    fun containsElement(element: DataType): Boolean;

    fun containsElementFromSet(set: Set<DataType>) : Boolean

    fun containsAllElementsFromSet(set: Set<DataType>) : Boolean

    fun containsAllElementsFromOneOf(set: Set<LoopTableEntryRestriction<DataType>>) : Boolean

    /**
     * returns true if this is a subset of the given restriction
     */
    fun isSubsetOf(restriction: LoopTableEntryRestriction<DataType>) : Boolean

    /**
     * returns true if this is a superset of the given restriction
     */
    fun isSupersetOf(restriction: LoopTableEntryRestriction<DataType>) : Boolean

    fun addElement(element: OWLClass): Boolean
}