package tinDB.model.v2.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdgeLabel
import tinLIB.model.v2.graph.EdgeLabelProperty

class DbResultEdgeLabel(
    val incoming: EdgeLabelProperty,
    val outgoing: EdgeLabelProperty,
    cost: Int,

    ): ResultEdgeLabel(cost) {

}