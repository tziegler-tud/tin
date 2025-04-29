package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultGraph
import tinLIB.model.v2.ResultGraph.ResultNode

interface ResultGraphBuilder<T: ResultNode, E: ResultEdge> {
    //construct GuA -> product graph consisting of nodes (q, t, Ind)
    fun constructRestrictedGraph() : ResultGraph<T,E>

    fun constructResultGraph() : ResultGraph<T,E>
}