package tin.services.ontology.loopTable.loopTableEntry.ELHI

import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction

class SPANumericLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: NumericConceptNameRestriction,
    ) : ELHISPALoopTableEntry(source, target, restriction) {
}