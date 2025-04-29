package tinLIB.services.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultGraphIndividual
import tinLIB.model.v2.ResultGraph.ResultNode

interface ResultGraphSolver<T: ResultNode> {

    fun getShortestPath(source: ResultGraphIndividual, target: ResultGraphIndividual) : ShortestPathResult<T>?

    fun getDistance(sourceNode: T, targetNode: T) : ShortestPathResult<T>?
}