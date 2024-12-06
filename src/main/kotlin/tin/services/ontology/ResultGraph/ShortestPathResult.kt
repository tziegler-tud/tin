package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.ResultGraph.ResultNode

class ShortestPathResult(
    private val source: ResultNode,
    private val target: ResultNode,
    private val cost: Int?,
)