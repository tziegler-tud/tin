package tin.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLIndividual

class AnonymousIndividualRestriction(
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

    override fun asSet(): Set<OWLIndividual> {
        return setOf(value);
    }

    override fun asList(): List<OWLIndividual> {
        return setOf(value).toList();
    }

}