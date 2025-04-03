package tin.services.ontology.ResultGraph

import tin.model.v2.ResultGraph.ResultEdge
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.Task.Benchmark.TaskProcessingResultBuilderStats
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class ELHIResultGraphBuilder(
    private val ec: ELHIExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) : AbstractResultGraphBuilder(ec, queryGraph, transducerGraph) {

    fun constructResultGraph(spTable: ELHISPLoopTable) : ResultGraph {
        val resultGraph = constructRestrictedGraph();
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                queryGraph.nodes.forEach { targetQueryNode ->
                    transducerGraph.nodes.forEach transducerTarget@{ targetTransducerNode ->
                        ec.forEachIndividual { individual ->
                            //get sp value
                            val restriction = ec.spRestrictionBuilder.createNamedIndividualRestriction(individual);
                            val entry = IndividualLoopTableEntry(
                                Pair(queryNode, transducerNode),
                                Pair(targetQueryNode, targetTransducerNode),
                                restriction
                            );
                            val cost = spTable.get(entry);
                            if (cost != null) {
                                val sourceNode = ResultNode(queryNode, transducerNode, individual);
                                val targetNode = ResultNode(targetQueryNode, targetTransducerNode, individual);
                                val edge = ResultEdge(sourceNode, targetNode, cost)
                                resultGraph.addEdge(edge);
                            }
                        }
                    }
                }
            }
        }

        return resultGraph
    }

    fun getStats(resultGraph: ResultGraph) : TaskProcessingResultBuilderStats {
        return TaskProcessingResultBuilderStats(
            resultGraph.nodes.size,
            resultGraph.edges.size,
            resultGraph.edges.maxOf { it.label.cost },
            resultGraph.edges.minOf { it.label.cost },
            findUnreachableNodes(resultGraph).size
        )
    }

    private fun findUnreachableNodes(resultGraph: ResultGraph) : List<ResultNode> {
        var counter: Int = 0;
        val unreachableNodes: MutableList<ResultNode> = mutableListOf()
        val nodesQueue: MutableList<ResultNode> = resultGraph.nodes.asList().toMutableList();
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