package tinDB.services.ontology.ResultGraph

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinDB.model.v2.ResultGraph.DbResultEdge
import tinDB.model.v2.ResultGraph.DbResultGraph
import tinDB.model.v2.ResultGraph.DbResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph

import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.services.ontology.ResultGraph.AbstractResultGraphBuilder

class DbResultGraphBuilder(
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val databaseGraph: DatabaseGraph

    ) : AbstractResultGraphBuilder<DbResultNode, DbResultEdge>(queryGraph, transducerGraph) {

    fun constructResultGraph() : DbResultGraph {

        val incomingEpsilonEdges = HashSet<TransducerEdge>()
        val propertyTransducerEdges = HashSet<TransducerEdge>()
        val fittingTransducerEdges = HashSet<TransducerEdge>()


        var resultGraph = DbResultGraph()
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode: Node ->

                if(queryGraph.nodes.isEmpty()) {
                    // find all incoming epsilon edges
                    // bandaid fix to insert incoming epsilon edges to fitting transducer edge every iteration.
                    transducerGraph.edges.forEach { transducerEdge ->
                        val incomingLabel = transducerEdge.label.incoming
                        if (incomingLabel.isEpsilonLabel()) {
                            incomingEpsilonEdges.add(transducerEdge)
                        }
                    }
                }


                queryGraph.nodes.forEach { targetQueryNode ->
                    transducerGraph.nodes.forEach transducerTarget@{ targetTransducerNode: Node ->


                        }
                }
            }
        }
        return resultGraph
    }
}