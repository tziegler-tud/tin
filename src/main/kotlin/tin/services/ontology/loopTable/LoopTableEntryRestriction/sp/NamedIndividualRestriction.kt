package tin.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual

class NamedIndividualRestriction(
    override var value: OWLIndividual
) : IndividualLoopTableEntryRestriction {
    override fun isElement(owlIndividual: OWLIndividual) : Boolean {
        return owlIndividual == value;
    }

    override fun isContainedInSet(set: Set<OWLIndividual>): Boolean {
        return set.contains(value)
    }

    override fun setElement(owlIndividual: OWLIndividual): Boolean {
        value = owlIndividual;
        return true;
    }

    override fun isEmpty(): Boolean {
        return false;
    }

}