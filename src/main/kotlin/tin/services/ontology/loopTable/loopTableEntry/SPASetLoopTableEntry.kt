package tin.services.ontology.loopTable.loopTableEntry

import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.NumericConceptNameRestriction

class SPASetLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: ConceptNameRestriction,
    ) : ELHISPALoopTableEntry(source, target, restriction) {
}