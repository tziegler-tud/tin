package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass

interface SetLoopTableEntryRestriction : MultiClassLoopTableEntryRestriction {
    override val value: Set<OWLClass>
}