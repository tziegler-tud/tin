package tin.services.ontology.loopTable.LoopTableEntryRestriction
import org.semanticweb.owlapi.model.OWLClass

class ConceptNameRestriction() : LoopTableEntryRestriction<OWLClass> {

    override val value: MutableSet<OWLClass> = hashSetOf();

    override fun asSet(): MutableSet<OWLClass> {
        return value;
    }

    override fun asList(): List<OWLClass> {
        return value.toList();
    }

    override fun containsElement(element: OWLClass) : Boolean {
        return value.contains(element);
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