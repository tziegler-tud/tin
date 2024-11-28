package tin.services.ontology.loopTable.loopTableEntry.ELHI

import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

open class ELHISPALoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: MultiClassLoopTableEntryRestriction,
    ) : AbstractLoopTableEntry(source, target, restriction) {
    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: MultiClassLoopTableEntryRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)
}