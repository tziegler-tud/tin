package tinDB.model.v2.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultEdgeLabel

class DbResultEdge(
    override val source: DbResultNode,
    override val target: DbResultNode,
    override val label: ResultEdgeLabel
): ResultEdge(
    source,
    target,
    label
)
{
    constructor(source: DbResultNode, target: DbResultNode, cost: Int) : this(source, target, ResultEdgeLabel(cost))

}