package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.util.ShortFormProvider
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

class ConceptNameRestriction() : MultiClassLoopTableEntryRestriction {

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

    override fun containsElementFromSet(set: Set<OWLClass>) : Boolean {
        return value.any { o -> set.contains(o) };
    }

    override fun containsAllElementsFromSet(set: Set<OWLClass>) : Boolean {
        set.forEach set@{ owlClass ->
            if(!this.containsElement(owlClass)) return@set;
            return true;
        }
        return false;
    }

    override fun containsAllElementsFromOneOf(set: Set<MultiClassLoopTableEntryRestriction>) : Boolean {
        set.forEach set@{ res ->
            if(res == this || this.isSupersetOf(res)) return true
        }
        return false;
    }

    override fun containsOnlyElementsFromOneOf(set: Set<MultiClassLoopTableEntryRestriction>) : Boolean {
        set.forEach set@{ res ->
            if(res == this || this.isSubsetOf(res)) return true
        }
        return false;
    }

    /**
     * returns true if this is a subset of the given restriction
     */
    override fun isSubsetOf(restriction: MultiClassLoopTableEntryRestriction) : Boolean {
        if(restriction !is ConceptNameRestriction) throw Error("Unable to perform this operation on objects other than ConceptNameRestriction!")
        return restriction.value.containsAll(this.value);
    }

    /**
     * returns true if this is a superset of the given restriction
     */
    override fun isSupersetOf(restriction: MultiClassLoopTableEntryRestriction) : Boolean {
        if(restriction !is ConceptNameRestriction) throw Error("Unable to perform this operation on objects other than ConceptNameRestriction!")
        return value.containsAll(restriction.value)
    }

    override fun addElement(element: OWLClass): Boolean {
        return value.add(element);
    }

    override fun getSize(): Int {
        return value.size;
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

    override fun transformToString(shortFormProvider: ShortFormProvider): String {
        if(isEmpty()) return "()";
        var bits: MutableSet<String> = mutableSetOf()
        this.asSet().forEach { owlClass ->
            bits.add(shortFormProvider.getShortForm(owlClass));
        }
        return bits.joinToString();
    }
}