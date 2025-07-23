package tinDB.model.v2.productAutomaton

import tinLIB.model.v2.ResultGraph.ResultEdgeLabel
import tinLIB.model.v2.graph.EdgeLabelProperty

class ProductAutomatonEdgeLabel(
    val incoming: EdgeLabelProperty,
    val outgoing: EdgeLabelProperty,
    cost: Int,
    ): ResultEdgeLabel(cost) {

        override fun toString(): String {
            return "[$incoming, $outgoing, $cost]"
        }

}