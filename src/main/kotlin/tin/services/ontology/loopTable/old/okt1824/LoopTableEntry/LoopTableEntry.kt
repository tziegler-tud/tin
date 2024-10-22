package tin.services.ontology.loopTable.old.okt1824.LoopTableEntry

import org.semanticweb.owlapi.model.OWLClass
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface LoopTableEntry {
    val source: Pair<Node, Node>;
    val target: Pair<Node, Node>
    val restriction: LoopTableEntryRestriction<OWLClass>

    fun hasEqualSourceAndTarget() : Boolean
}