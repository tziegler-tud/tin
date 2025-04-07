package tinDL.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tinDL.model.v2.ResultGraph.ResultGraph
import tinDL.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.genericGraph.GenericGraph
import tinLIB.model.v2.graph.Node

class DijkstraSolver(private val resultGraph: ResultGraph) : ResultGraphSolver {

    override fun getShortestPath(source: OWLNamedIndividual, target: OWLNamedIndividual) : ShortestPathResult?
    {
        //get all initial nodes containing source
        val sourceNodes = resultGraph.getInitialNodes(source);
        val targetNodes = resultGraph.getFinalNodes(target);

        val resultSet:  MutableSet<ShortestPathResult> = mutableSetOf();

        sourceNodes.forEach { sourceN ->
            val sourceNode = sourceN.asResultNode()!!
            targetNodes.forEach { targetN ->
                val targetNode = targetN.asResultNode()!!
                //get distance
                val distance = getDistance(sourceNode, targetNode).cost
                if(distance != null) {
                    val minCandidate = ShortestPathResult(sourceNode, targetNode, distance);
                    resultSet.add(minCandidate);
                }
            }
        }
        if(resultSet.isEmpty()) return null
        resultSet.sortedBy { it.cost }
        return resultSet.first();
    }

    override fun getDistance(sourceNode: ResultNode, targetNode: ResultNode) : ShortestPathResult
    {
        return dijkstra(resultGraph, sourceNode, targetNode)
    }

    private fun dijkstra(graph: ResultGraph, source: ResultNode, target: ResultNode) : ShortestPathResult {
        // Note: this implementation uses similar variable names as the algorithm given do.
        // We found it more important to align with the algorithm than to use possibly more sensible naming.

        val dist = mutableMapOf<ResultNode, Int?>()
        val prev = mutableMapOf<ResultNode, ResultNode?>()
        val q = DijkstraQueue();
        val edges = graph.edges

        graph.nodes.forEach { n ->
            val v = n.asResultNode()!!;
            dist[v] = Integer.MAX_VALUE
            prev[v] = null
            q.add(v)
        }
        dist[source] = 0

        while (q.isNotEmpty()) {
            val u = q.minBy { dist[it]!! }
            q.removeNode(u);

            if (u == target) {
                break // Found shortest path
            }
            edges
                .filter { it.source == u }
                .forEach { edge ->
                    val v = edge.target
                    val alt = (dist[u] ?: 0) + edge.label.cost;
                    if (alt < (dist[v] ?: 0)) {
                        dist[v] = alt
                        prev[v] = u
                    }
                }
        }

        return ShortestPathResult(source, target, dist[target])
    }
}