package tin.services.ontology.loopTable.LoopTableEntryRestriction
import org.semanticweb.owlapi.model.OWLClass

class NumericConceptNameRestriction() : NumericLoopTableEntryRestriction<OWLClass> {

    override val value: Long = 0;

    override fun asSet(): MutableSet<OWLClass> {

    }

    override fun isEmpty(): Boolean {
        return value == 0L;
    }

    override fun asList(): List<OWLClass> {
    }

    override fun containsElement(element: OWLClass) : Boolean {
    }

    override fun containsElement(long: Long) : Boolean {
    }

    fun containsElementFromSet(set: Set<OWLClass>) : Boolean {

    }

    fun containsAllElementsFromSet(set: Set<NumbericConceptNameRestriction>) : Boolean {

    }

    /**
     * returns true if this is a subset of the given restriction
     */
    fun isSubsetOf(restriction: NumbericConceptNameRestriction) : Boolean {
    }

    /**
     * returns true if this is a superset of the given restriction
     */
    fun isSupersetOf(restriction: NumbericConceptNameRestriction) : Boolean {
    }

    fun addElement(element: OWLClass): Boolean {
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NumbericConceptNameRestriction) {
            return false;
        }
        return value == other.value;
    }

    override fun hashCode(): Int {
        return value.hashCode();
    }
}