package tinDB.model.v2.ResultGraph

import tinDB.model.v2.productAutomaton.ProductAutomatonEdge
import tinLIB.model.v2.ResultGraph.ResultEdge

class DbResultEdge(
    override val source: DbResultNode,
    override val target: DbResultNode,
    override val label: DbResultEdgeLabel
): ResultEdge(
    source,
    target,
    label
) {
    constructor(productAutomatonEdge: ProductAutomatonEdge) : this(
        source = DbResultNode(productAutomatonEdge.source),
        target = DbResultNode(productAutomatonEdge.target),
        label = DbResultEdgeLabel(productAutomatonEdge.label)
    )
}