package tinDB.model.v2.ResultGraph

import tinDB.model.v2.productAutomaton.ProductAutomatonEdgeLabel
import tinLIB.model.v2.ResultGraph.ResultEdgeLabel
import tinLIB.model.v2.graph.EdgeLabelProperty

class DbResultEdgeLabel(
    val incoming: EdgeLabelProperty,
    val outgoing: EdgeLabelProperty,
    cost: Int,

    ): ResultEdgeLabel(cost) {

        constructor(productAutomatonEdgeLabel: ProductAutomatonEdgeLabel) : this(
            productAutomatonEdgeLabel.incoming,
            productAutomatonEdgeLabel.outgoing,
            productAutomatonEdgeLabel.cost
        )
}