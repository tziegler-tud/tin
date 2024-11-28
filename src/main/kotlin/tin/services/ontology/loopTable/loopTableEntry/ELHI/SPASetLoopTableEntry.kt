package tin.services.ontology.loopTable.loopTableEntry.ELHI

import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction

class SPASetLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: ConceptNameRestriction,
    ) : ELHISPALoopTableEntry(source, target, restriction) {
}