package tin.services.ontology.loopTable.LoopTableEntryRestriction.sp

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface IndividualLoopTableEntryRestriction : LoopTableEntryRestriction {

    override var value: OWLIndividual;

    fun isElement(owlIndividual: OWLIndividual) : Boolean

    fun isContainedInSet(set: Set<OWLIndividual>): Boolean

    fun setElement(owlIndividual: OWLIndividual) : Boolean;

}