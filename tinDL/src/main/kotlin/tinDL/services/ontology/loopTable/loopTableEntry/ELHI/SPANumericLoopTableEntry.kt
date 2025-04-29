package tinDL.services.ontology.loopTable.loopTableEntry.ELHI

import tinLIB.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction

class SPANumericLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: NumericConceptNameRestriction,
    ) : ELHISPALoopTableEntry(source, target, restriction) {
    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: NumericConceptNameRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)
}