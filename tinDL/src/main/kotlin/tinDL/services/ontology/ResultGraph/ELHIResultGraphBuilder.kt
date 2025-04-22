package tinDL.services.ontology.ResultGraph

import tinDL.model.v2.ResultGraph.*

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.Task.Benchmark.TaskProcessingResultBuilderStats
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import tinLIB.model.v2.graph.Node
import tinLIB.services.ontology.ResultGraph.AbstractResultGraphBuilder

class ELHIResultGraphBuilder(
    private val ec: ELHIExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) : AbstractDlResultGraphBuilder(ec, queryGraph, transducerGraph) {

    fun constructResultGraph(spTable: ELHISPLoopTable) : DlResultGraph {
        val resultGraph = constructRestrictedGraph();
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode: Node ->
                queryGraph.nodes.forEach { targetQueryNode ->
                    transducerGraph.nodes.forEach transducerTarget@{ targetTransducerNode: Node ->
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
                                val sourceNode = DlResultNode(queryNode, transducerNode, individual);
                                val targetNode = DlResultNode(targetQueryNode, targetTransducerNode, individual);
                                val edge = DlResultEdge(sourceNode, targetNode, cost)
                                resultGraph.addEdge(edge);
                            }
                        }
                    }
                }
            }
        }

        return resultGraph
    }
}