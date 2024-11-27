package tin.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface IndividualLoopTableEntryRestriction : LoopTableEntryRestriction {

    override var value: OWLNamedIndividual;

    fun isElement(owlIndividual: OWLNamedIndividual) : Boolean

    fun isContainedInSet(set: Set<OWLNamedIndividual>): Boolean

    fun setElement(owlIndividual: OWLNamedIndividual) : Boolean;

}