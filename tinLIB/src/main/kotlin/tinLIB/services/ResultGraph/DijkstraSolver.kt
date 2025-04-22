package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultGraph
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.ResultGraph.ResultGraphIndividual

open class DijkstraSolver<T: ResultNode, E: ResultEdge>(private val resultGraph: ResultGraph<T, E>) : ResultGraphSolver<T> {

    override fun getShortestPath(source: ResultGraphIndividual, target: ResultGraphIndividual) : ShortestPathResult<T>?
    {
        //get all initial nodes containing source
        val sourceNodes = resultGraph.getInitialNodes(source);
        val targetNodes = resultGraph.getFinalNodes(target);

        val resultSet:  MutableSet<ShortestPathResult<T>> = mutableSetOf();

        sourceNodes.forEach { sourceNode ->
            targetNodes.forEach { targetNode ->
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

    override fun getDistance(sourceNode: T, targetNode: T) : ShortestPathResult<T>
    {
        return dijkstra(resultGraph, sourceNode, targetNode)
    }

    private fun dijkstra(graph: ResultGraph<T,E>, source: T, target: T) : ShortestPathResult<T> {
        // Note: this implementation uses similar variable names as the algorithm given do.
        // We found it more important to align with the algorithm than to use possibly more sensible naming.

        val dist = mutableMapOf<T, Int?>()
        val prev = mutableMapOf<T, T?>()
        val q = DijkstraQueue<T>();
        val edges = graph.edges

        graph.nodes.forEach { n ->
            val v = n
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
                .filterForSource(u)
                .forEach { edge: E ->
                    val v = edge.target
                    val alt = (dist[u] ?: 0) + edge.label.cost;
                    if (alt < (dist[v] ?: 0)) {
                        dist[v as T] = alt
                        prev[v as T] = u
                    }
                }
        }

        return ShortestPathResult(source, target, dist[target])
    }
}