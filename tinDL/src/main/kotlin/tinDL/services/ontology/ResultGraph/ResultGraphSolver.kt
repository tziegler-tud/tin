package tinDL.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tinDL.model.v2.ResultGraph.ResultGraph
import tinDL.model.v2.ResultGraph.ResultNode
import tinDL.model.v2.genericGraph.GenericGraph
import tinDL.model.v2.graph.Node

interface ResultGraphSolver {

    fun getShortestPath(source: OWLNamedIndividual, target: OWLNamedIndividual) : ShortestPathResult?

    fun getDistance(sourceNode: ResultNode, targetNode: ResultNode) : ShortestPathResult?
}