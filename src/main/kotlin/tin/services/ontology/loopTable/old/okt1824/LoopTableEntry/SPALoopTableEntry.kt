package tin.services.ontology.loopTable.old.okt1824.LoopTableEntry

import tin.model.v2.graph.Node
import tin.model.v2.query.QueryGraph
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction

class SPALoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: ConceptNameRestriction,
    ) : LoopTableEntry {

    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: ConceptNameRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)

    override fun hasEqualSourceAndTarget(): Boolean {
        return (source.first == target.first && source.second == target.second)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SPALoopTableEntry) return false

        return source == other.source && target == other.target && restriction == other.restriction;
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + restriction.hashCode()
        return result;
    }
}