package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v1.queryResult.DLQueryResult.DLQueryResult
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.genericGraph.GenericGraph
import tin.model.v2.graph.Node

class ResultGraphSolver(private val resultGraph: ResultGraph) {
    private var distanceMap: HashMap<Pair<ResultNode, ResultNode>, Int> = floydWarshall(resultGraph);


    fun getShortestPath(source: OWLNamedIndividual, target: OWLNamedIndividual) : ShortestPathResult
    {
        //get all initial nodes containing source
        val sourceNodes = resultGraph.getInitialNodes(source);
        val targetNodes = resultGraph.getFinalNodes(target);

        val resultSet:  MutableSet<ShortestPathResult> = mutableSetOf();

        sourceNodes.forEach { sourceNode ->
            targetNodes.forEach { targetNode ->
                //get distance
                val distance = distanceMap[Pair(sourceNode.asResultNode()!!, targetNode.asResultNode()!!)];
                if(distance != null) {
                    val minCandidate = ShortestPathResult(sourceNode.asResultNode()!!, targetNode.asResultNode()!!, distance);
                    resultSet.add(minCandidate);
                }
            }
        }
        resultSet.sortedBy { it.cost }
        return resultSet.first();
    }

    fun getDistance(sourceNode: ResultNode, targetNode: ResultNode) : Int?
    {
        return distanceMap[Pair(sourceNode, targetNode)]
    }

    fun getAllShortestPaths() : List<ShortestPathResult> {
        val resultList: MutableList<ShortestPathResult> = mutableListOf();
        distanceMap.forEach { (k, v) ->
            val source: ResultNode = k.first;
            val target: ResultNode = k.second;
            val cost: Int = v;
            if(source.isInitialState && target.isFinalState)  resultList.add(ShortestPathResult(source, target, cost));
        }
        return resultList
    }

    fun getShortestPathMap() : HashMap<Pair<ResultNode, ResultNode>, Int> {
        return distanceMap
    }

    private fun floydWarshall(graph: ResultGraph): HashMap<Pair<ResultNode, ResultNode>, Int> {

        var distance: HashMap<Pair<ResultNode, ResultNode>, Int> = HashMap();

        graph.edges.forEach {
            distance[Pair(it.source, it.target)] = it.label.cost;
        }

        graph.nodes.forEach { it: ResultNode ->
            distance[Pair(it, it)] = 0;
        }

        for (kNode in graph.nodes) {
            for (iNode in graph.nodes) {
                for (jNode in graph.nodes) {
                    val k = kNode.asResultNode()!!;
                    val i = iNode.asResultNode()!!;
                    val j = jNode.asResultNode()!!;
                    if(distance[Pair(i,k)] != null && distance[Pair(k,j)] != null) {
                        val dist = distance[Pair(i,k)]!! + distance[Pair(k,j)]!!;
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