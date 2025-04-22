package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultNode

class ShortestPathResult<T: ResultNode>(
    val source: T,
    val target: T,
    val cost: Int?,
) {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is ShortestPathResult<*>) return false
        return source == other.source && target == other.target && cost == other.cost;
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        if(cost !==null) result = 31 * result + cost.hashCode()
        return result
    }
}