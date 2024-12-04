package tin.services.ontology.ResultGraph

import tin.model.v2.query.QueryGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext

interface ResultGraphBuilder {
    //construct GuA -> product graph consisting of nodes (q, t, Ind)
    fun constructRestrictedGraph()
}