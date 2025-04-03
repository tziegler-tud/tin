package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.ResultGraph.ResultNode

class ShortestPathResult(
    val source: ResultNode,
    val target: ResultNode,
    val cost: Int?,
) {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is ShortestPathResult) return false
        return source == other.source && target == other.target && cost == other.cost;
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        if(cost !==null) result = 31 * result + cost.hashCode()
        return result
    }
}