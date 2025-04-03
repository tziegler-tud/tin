package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.util.ShortFormProvider

class NamedIndividualRestriction(
    override var value: OWLNamedIndividual
) : IndividualLoopTableEntryRestriction {
    override fun isElement(owlIndividual: OWLNamedIndividual) : Boolean {
        return owlIndividual == value;
    }

    override fun isContainedInSet(set: Set<OWLNamedIndividual>): Boolean {
        return set.contains(value)
    }

    override fun setElement(owlIndividual: OWLNamedIndividual): Boolean {
        value = owlIndividual;
        return true;
    }

    override fun isEmpty(): Boolean {
        return false;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true;
        if(other !is NamedIndividualRestriction) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun transformToString(shortFormProvider: ShortFormProvider): String {
        return shortFormProvider.getShortForm(value)
    }
}