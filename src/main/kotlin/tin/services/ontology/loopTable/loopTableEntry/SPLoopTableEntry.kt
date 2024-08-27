package tin.services.ontology.loopTable.loopTableEntry

import tin.model.query.QueryNode
import tin.model.transducer.TransducerNode
import tin.services.ontology.loopTable.LoopTableEntryRestriction.AboxIndividualRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction

class SPLoopTableEntry(
    override val source: Pair<QueryNode, TransducerNode>,
    override val target: Pair<QueryNode, TransducerNode>,
    override val restriction: AboxIndividualRestriction,
) : LoopTableEntry {

    override fun hasEqualSourceAndTarget(): Boolean {
        return (source.first == target.first && source.second == target.second)
    }
}