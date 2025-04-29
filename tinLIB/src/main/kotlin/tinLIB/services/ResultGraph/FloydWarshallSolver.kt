package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultGraph
import tinLIB.model.v2.ResultGraph.ResultGraphIndividual
import tinLIB.model.v2.ResultGraph.ResultNode

class FloydWarshallSolver<T: ResultNode,E: ResultEdge>(private val resultGraph: ResultGraph<T,E>) : ResultGraphSolver<T> {
    private var distanceMap: HashMap<Pair<T, T>, Int> = floydWarshall(resultGraph);

    override fun getShortestPath(source: ResultGraphIndividual, target: ResultGraphIndividual) : ShortestPathResult<T>?
    {
        //get all initial nodes containing source
        val sourceNodes = resultGraph.getInitialNodes(source);
        val targetNodes = resultGraph.getFinalNodes(target);

        val resultSet:  MutableSet<ShortestPathResult<T>> = mutableSetOf();

        sourceNodes.forEach { sourceNode ->
            targetNodes.forEach { targetNode ->
                //get distance
                val distance = distanceMap[Pair(sourceNode as ResultNode, targetNode as ResultNode)];
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

    override fun getDistance(sourceNode: T, targetNode: T) : ShortestPathResult<T> {
        return ShortestPathResult(sourceNode, targetNode, getDistanceMapEntry(sourceNode, targetNode))
    }

    private fun getDistanceMapEntry(sourceNode: ResultNode, targetNode: ResultNode) : Int?
    {
        return distanceMap[Pair(sourceNode, targetNode)]
    }

    fun getAllShortestPaths() : List<ShortestPathResult<T>> {
        val resultList: MutableList<ShortestPathResult<T>> = mutableListOf();
        distanceMap.forEach { (k, v) ->
            val source: T = k.first;
            val target: T = k.second;
            val cost: Int = v;
            if(source.isInitialState && target.isFinalState)  resultList.add(ShortestPathResult<T>(source, target, cost));
        }
        return resultList
    }

    fun getAllShortestPathsWithMaxCost(maxCost: Int) : List<ShortestPathResult<T>> {
        val resultList: MutableList<ShortestPathResult<T>> = mutableListOf();
        distanceMap.forEach { (k, v) ->
            val source = k.first;
            val target = k.second;
            val cost: Int = v;
            if(source.isInitialState && target.isFinalState && cost<=maxCost)  resultList.add(ShortestPathResult<T>(source, target, cost));
        }
        return resultList
    }

    fun getShortestPathMap() : HashMap<Pair<T, T>, Int> {
        return distanceMap
    }

    private fun floydWarshall(graph: ResultGraph<T,E>): HashMap<Pair<T, T>, Int> {

        var distance: HashMap<Pair<T, T>, Int> = HashMap();

        graph.edges.forEach { it: E ->
            distance[Pair(it.source as T, it.target as T)] = it.label.cost;
        }

        graph.nodes.forEach { it: T ->
            distance[Pair(it, it)] = 0;
        }

        for (kNode in graph.nodes) {
            for (iNode in graph.nodes) {
                val k = kNode as T;
                val i = iNode as T;
                val distIK = distance[Pair(i,k)] ?: continue;

                for (jNode in graph.nodes) {
                    val j = jNode as T;
                    val kj = Pair(k,j)
                    val distkj = distance[kj]
                    if(distkj != null) {
                        val dist = distIK + distkj;
                        if(distance.contains(Pair(i,j))) {
                            if (distance[Pair(i,j)]!! > dist) distance[Pair(i,j)] = dist;
                        }
                        else distance[Pair(i,j)] = dist;
                    }
                }
            }
        }
        return distance;
    }
}