package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.genericGraph.GenericGraph
import tin.model.v2.graph.Node

interface ResultGraphSolver {

    fun getShortestPath(source: OWLNamedIndividual, target: OWLNamedIndividual) : ShortestPathResult?

    fun getDistance(sourceNode: ResultNode, targetNode: ResultNode) : ShortestPathResult?
}