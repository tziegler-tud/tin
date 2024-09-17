package tin.services.ontology.loopTable.loopTableEntry

import org.semanticweb.owlapi.model.OWLClass
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface LoopTableEntry {
    val source: Pair<Node, Node>;
    val target: Pair<Node, Node>
    val restriction: LoopTableEntryRestriction<OWLClass>

    fun hasEqualSourceAndTarget() : Boolean
}