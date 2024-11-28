package tin.services.ontology.loopTable.loopTableEntry

import org.semanticweb.owlapi.util.ShortFormProvider
import tin.model.v2.graph.Node
import tin.services.ontology.loopTable.LoopTableEntryRestriction.sp.NamedIndividualRestriction

class IndividualLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: NamedIndividualRestriction,
) : AbstractLoopTableEntry(source, target, restriction)

{
    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: NamedIndividualRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IndividualLoopTableEntry) return false
        return super.equals(other);
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + restriction.hashCode()
        return result;
    }
}