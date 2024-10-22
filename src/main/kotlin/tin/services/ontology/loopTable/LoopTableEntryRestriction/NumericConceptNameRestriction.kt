package tin.services.ontology.loopTable.LoopTableEntryRestriction
import org.semanticweb.owlapi.model.OWLClass
import java.math.BigInteger
import kotlin.math.pow

class NumericConceptNameRestriction(
    private val numericSetUtility: NumericSetUtility,
    override var value: ULong = 0UL

) : NumericLoopTableEntryRestriction<OWLClass> {

    constructor(res: NumericConceptNameRestriction) : this(res.numericSetUtility, res.value)

    override fun asSet(): Set<OWLClass> {
        return numericSetUtility.getClassSet(value);
    }

    override fun isEmpty(): Boolean {
        return value == 0UL;
    }

    override fun asList(): List<OWLClass> {
        return asSet().toList();
    }

    override fun containsElement(element: OWLClass) : Boolean {
        return numericSetUtility.containsElement(value, element);
    }

    fun containsElementFromSet(set: Set<OWLClass>) : Boolean {
        set.forEach { owlClass ->
            if(containsElement(owlClass)) return true;
        }
        return false;
    }

    fun containsAllElementsFromSet(set: Set<OWLClass>) : Boolean {
        val repr = numericSetUtility.getSetAsSetRepresentation(set);
        return numericSetUtility.containsElement(value, repr);
    }

    /**
     * returns true if this is a subset of the given restriction
     */
    fun isSubsetOf(restriction: NumericConceptNameRestriction) : Boolean {
        return numericSetUtility.containsElement(restriction.value, value);
    }

    /**
     * returns true if this is a superset of the given restriction
     */
    fun isSupersetOf(restriction: NumericConceptNameRestriction) : Boolean {
        return numericSetUtility.containsElement(value, restriction.value);
    }

    fun addElement(element: OWLClass): Boolean {
        val ojVal = value;
        value = numericSetUtility.addElement(value, element)
        return ojVal != value;
    }

    fun removeElement(element: OWLClass): Boolean {
        val ojVal = value;
        value = numericSetUtility.removeElement(value, element)
        return ojVal != value;
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NumericConceptNameRestriction) {
            return false;
        }
        return value == other.value;
    }

    override fun hashCode(): Int {
        return value.hashCode();
    }


}