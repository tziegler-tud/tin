package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.ResultGraph.ResultNode

class ShortestPathResult(
    source: ResultNode,
    target: ResultNode,
    cost: Int,
)