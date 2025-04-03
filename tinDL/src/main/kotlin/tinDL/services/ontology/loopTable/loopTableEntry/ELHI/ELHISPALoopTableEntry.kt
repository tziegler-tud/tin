package tinDL.services.ontology.loopTable.loopTableEntry.ELHI

import tinDL.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

open class ELHISPALoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: MultiClassLoopTableEntryRestriction,
    ) : tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry(source, target, restriction) {
    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: MultiClassLoopTableEntryRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)
}