package tinDB.services.ResultGraph

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinDB.model.v2.ResultGraph.DbResultEdge
import tinDB.model.v2.ResultGraph.DbResultGraph
import tinDB.model.v2.ResultGraph.DbResultNode

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.services.ontology.ResultGraph.AbstractResultGraphBuilder
import tinLIB.services.ontology.ResultGraph.ResultGraphBuilderStats

abstract class AbstractDbResultGraphBuilder(
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val databaseGraph: DatabaseGraph,

    ) : AbstractResultGraphBuilder<DbResultNode, DbResultEdge>(queryGraph, transducerGraph) {

    fun getStats(resultGraph: DbResultGraph) : ResultGraphBuilderStats {
        return ResultGraphBuilderStats(
            resultGraph.nodes.size,
            resultGraph.edges.size,
            resultGraph.edges.maxOf { it.label.cost },
            resultGraph.edges.minOf { it.label.cost },
            findUnreachableNodes(resultGraph).size
        )
    }

    private fun findUnreachableNodes(resultGraph: DbResultGraph) : List<DbResultNode> {
        var counter: Int = 0;
        val unreachableNodes: MutableList<DbResultNode> = mutableListOf()
        val nodesQueue: MutableList<DbResultNode> = resultGraph.nodes.asList().toMutableList();
        //a node is unreachable if it has no incoming edges from a node different from itself.
        if(nodesQueue.size <= 1) return unreachableNodes;
        nodesQueue.forEach { node ->
            val edges = resultGraph.getEdgesWithTarget(node);
            if(edges.isNotEmpty()) return@forEach;
            if(edges.filter{it.source !== node}.isNotEmpty()) return@forEach;
            unreachableNodes.add(node);
        }
        return unreachableNodes;
    }
}