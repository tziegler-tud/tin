package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultGraph
import tinLIB.model.v2.ResultGraph.ResultNode

interface ResultGraphBuilder<T: ResultNode, E: ResultEdge> {
    fun constructResultGraph() : ResultGraph<T,E>
}