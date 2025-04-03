package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.util.ShortFormProvider

class SingleClassConceptNameRestriction(
    override var value: OWLClass
) : SingleClassLoopTableEntryRestriction {

    override fun isElement(owlClass: OWLClass) : Boolean{
        return value == owlClass;
    }

    override fun isContainedInSet(set: Set<OWLClass>): Boolean {
        return set.contains(value);
    }

    override fun setElement(owlClass: OWLClass) {
        value = owlClass;
    }

    override fun isEmpty(): Boolean {
        return false;
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SingleClassConceptNameRestriction) {
            return false;
        }
        return value == other.value;
    }

    override fun hashCode(): Int {
        return value.hashCode();
    }

    override fun transformToString(shortFormProvider: ShortFormProvider): String {
        return shortFormProvider.getShortForm(value)
    }
}