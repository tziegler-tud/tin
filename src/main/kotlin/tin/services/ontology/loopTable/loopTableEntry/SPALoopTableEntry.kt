package tin.services.ontology.loopTable.loopTableEntry

import tin.model.query.QueryNode
import tin.model.transducer.TransducerNode
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction

class SPALoopTableEntry(
    override val source: Pair<QueryNode, TransducerNode>,
    override val target: Pair<QueryNode, TransducerNode>,
    override val restriction: ConceptNameRestriction,
    ) : LoopTableEntry {
}