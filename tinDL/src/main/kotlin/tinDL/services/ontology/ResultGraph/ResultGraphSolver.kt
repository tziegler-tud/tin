package tinDL.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tinDL.model.v2.ResultGraph.ResultNode

interface ResultGraphSolver {

    fun getShortestPath(source: OWLNamedIndividual, target: OWLNamedIndividual) : ShortestPathResult?

    fun getDistance(sourceNode: ResultNode, targetNode: ResultNode) : ShortestPathResult?
}