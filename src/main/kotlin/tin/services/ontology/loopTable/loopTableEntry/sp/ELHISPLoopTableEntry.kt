package tin.services.ontology.loopTable.loopTableEntry.sp

import org.semanticweb.HermiT.model.Individual
import org.semanticweb.owlapi.model.OWLClass
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.sp.NamedIndividualRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.SPLoopTableEntry

class ELHISPLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: NamedIndividualRestriction,
    ) : SPLoopTableEntry(source, target, restriction) {
    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: NamedIndividualRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)


}