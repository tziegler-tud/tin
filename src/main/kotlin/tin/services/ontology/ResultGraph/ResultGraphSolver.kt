package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v1.queryResult.DLQueryResult.DLQueryResult
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode

class ResultGraphSolver {
    fun findShortestPath(sourceNode: ResultNode, targetNode: ResultNode, resultGraph: ResultGraph) : ShortestPathResult
    {
        return ShortestPathResult(sourceNode, targetNode, 0)
    }
}