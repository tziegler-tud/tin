package tin.services.ontology.loopTable.loopTableEntry

import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface LoopTableEntry {
    val source: Pair<Node, Node>;
    val target: Pair<Node, Node>
    val restriction: LoopTableEntryRestriction<Any>

    fun hasEqualSourceAndTarget() : Boolean
}