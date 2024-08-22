package tin.services.ontology.loopTable.loopTableEntry

import tin.model.query.QueryNode
import tin.model.transducer.TransducerNode
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction

interface LoopTableEntry {
    val source: Pair<QueryNode, TransducerNode>;
    val target: Pair<QueryNode, TransducerNode>
    val restriction: LoopTableEntryRestriction<Any>

    fun hasEqualSourceAndTarget() : Boolean
}