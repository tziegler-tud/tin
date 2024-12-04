package tin.services.ontology.ResultGraph

import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext

class ELHIResultGraphBuilder(
    private val ec: ELHIExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) : ResultGraphBuilder {

    /**
     * construct product graph restricted to nodes (s,t, a) a € Ind(A)
     *
     */
    override fun constructRestrictedGraph() {
        val graph = ResultGraph();


        //construct nodes
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                ec.forEachIndividual { individual ->
                    graph.addNode(ResultNode(queryNode, transducerNode, individual))
                }
            }
        }

        //add edges
        
    }
}