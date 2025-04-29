package tinDL.model.v2.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultEdgeLabel
import tinLIB.model.v2.ResultGraph.ResultNode

class DlResultEdge(
    override val source: DlResultNode,
    override val target: DlResultNode,
    override val label: ResultEdgeLabel
): ResultEdge(
    source,
    target,
    label
)
{
    constructor(source: DlResultNode, target: DlResultNode, cost: Int) : this(source, target, ResultEdgeLabel(cost))

}