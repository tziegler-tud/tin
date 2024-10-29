package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa
import org.semanticweb.owlapi.model.OWLClass
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

class NumericConceptNameRestriction(
    private val numericSetUtility: NumericSetUtility,
    override var value: ULong = 0UL

) : NumericLoopTableEntryRestriction {

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

    override fun containsElementFromSet(set: Set<OWLClass>) : Boolean {
        set.forEach { owlClass ->
            if(containsElement(owlClass)) return true;
        }
        return false;
    }

    override fun containsAllElementsFromSet(set: Set<OWLClass>) : Boolean {
        val repr = numericSetUtility.getSetAsSetRepresentation(set);
        return numericSetUtility.containsElement(value, repr);
    }

    override fun containsAllElementsFromOneOf(set: Set<LoopTableEntryRestriction<OWLClass>>) : Boolean {
        //TODO: Implement this in a performant way
        set.forEach set@{ res ->
            if(res == this) return true;
            res.asSet().forEach { owlClass ->
                if(!this.containsElement(owlClass)) return@set;
            }
            return true;
        }
        return false;
    }

    /**
     * returns true if this is a subset of the given restriction
     */
    override fun isSubsetOf(restriction: LoopTableEntryRestriction<OWLClass>) : Boolean {
        if(restriction !is NumericConceptNameRestriction) throw Error("Unable to perform this operation on objects other than NumericConceptNameRestriction!")
        return numericSetUtility.containsElement(restriction.value, value);
    }

    /**
     * returns true if this is a superset of the given restriction
     */
    override fun isSupersetOf(restriction: LoopTableEntryRestriction<OWLClass>) : Boolean {
        if(restriction !is NumericConceptNameRestriction) throw Error("Unable to perform this operation on objects other than NumericConceptNameRestriction!")
        return numericSetUtility.containsElement(value, restriction.value);
    }

    override fun addElement(element: OWLClass): Boolean {
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