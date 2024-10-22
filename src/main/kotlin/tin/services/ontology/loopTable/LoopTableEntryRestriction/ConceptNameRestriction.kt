package tin.services.ontology.loopTable.LoopTableEntryRestriction
import org.semanticweb.owlapi.model.OWLClass

class ConceptNameRestriction() : SetLoopTableEntryRestriction<OWLClass> {

    override val value: MutableSet<OWLClass> = hashSetOf();

    override fun asSet(): MutableSet<OWLClass> {
        return value;
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty();
    }

    override fun asList(): List<OWLClass> {
        return value.toList();
    }

    override fun containsElement(element: OWLClass) : Boolean {
        return value.contains(element);
    }

    fun containsElementFromSet(set: Set<OWLClass>) : Boolean {
        return value.any { o -> set.contains(o) };
    }

    fun containsAllElementsFromSet(set: Set<ConceptNameRestriction>) : Boolean {
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
    fun isSubsetOf(restriction: ConceptNameRestriction) : Boolean {
        return restriction.value.containsAll(this.value);
    }

    /**
     * returns true if this is a superset of the given restriction
     */
    fun isSupersetOf(restriction: ConceptNameRestriction) : Boolean {
        return value.containsAll(restriction.value)
    }

    fun addElement(element: OWLClass): Boolean {
        return value.add(element);
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ConceptNameRestriction) {
            return false;
        }
        return value == other.value;
    }

    override fun hashCode(): Int {
        return value.hashCode();
    }
}