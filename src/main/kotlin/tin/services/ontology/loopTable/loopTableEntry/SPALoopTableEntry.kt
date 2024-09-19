package tin.services.ontology.loopTable.loopTableEntry

import tin.model.v2.graph.Node
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
}