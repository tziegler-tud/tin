package tinDL.services.ontology.loopTable.loopTableEntry.ELH

import tinLIB.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

open class ELSPALoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: SingleClassLoopTableEntryRestriction,
    ) : tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry(source, target, restriction) {

    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: SingleClassLoopTableEntryRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)
}