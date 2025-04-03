package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface SingleClassLoopTableEntryRestriction : LoopTableEntryRestriction {

    override var value: OWLClass;

    fun isElement(owlClass: OWLClass): Boolean

    fun isContainedInSet(set: Set<OWLClass>): Boolean

    fun setElement(owlClass: OWLClass);

}