package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface SingleClassLoopTableEntryRestriction : LoopTableEntryRestriction {

    override var value: OWLClass;

    fun isElement(owlClass: OWLClass)

    fun isContainedInSet(set: Set<OWLClass>): Boolean

    fun setElement(owlClass: OWLClass) : Boolean;

}