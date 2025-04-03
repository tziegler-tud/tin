package tinDL.services.ontology.loopTable.loopTableEntry

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.util.ShortFormProvider
import tinDL.model.v2.graph.Node
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.ConceptNameRestriction

abstract class AbstractLoopTableEntry(
    override val source: Pair<Node, Node>,
    override val target: Pair<Node, Node>,
    override val restriction: LoopTableEntryRestriction,
) : tinDL.services.ontology.loopTable.loopTableEntry.LoopTableEntry {

    constructor(querySource: Node, transducerSource: Node, queryTarget: Node, transducerTarget: Node, restriction: LoopTableEntryRestriction)
            : this(Pair(querySource, transducerSource), Pair(queryTarget, transducerTarget), restriction)

    override fun hasEqualSourceAndTarget(): Boolean {
        return (source.first == target.first && source.second == target.second)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is tinDL.services.ontology.loopTable.loopTableEntry.AbstractLoopTableEntry) return false

        return source == other.source && target == other.target && restriction == other.restriction;
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + restriction.hashCode()
        return result;
    }

    override fun toString(): String {
        return "[(${source.first}, ${source.second}), (${target.first}, ${target.second}), $restriction]"
    }


    override fun transformToString(shortFormProvider: ShortFormProvider): String {
        return "[(${source.first}, ${source.second}), (${target.first}, ${target.second}), ${restriction.transformToString(shortFormProvider)}]"
    }
}