package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.ResultGraph.ResultNode

class ShortestPathResult(
    val source: ResultNode,
    val target: ResultNode,
    val cost: Int?,
)