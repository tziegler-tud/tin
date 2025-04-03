package tinDL.services.ontology.loopTable.loopTableEntry.ELHI

import tinDL.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction

class SPASetLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: ConceptNameRestriction,
    ) : ELHISPALoopTableEntry(source, target, restriction) {
}