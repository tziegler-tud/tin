package tinLIB.services.ontology.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultEdge
import tinLIB.model.v2.ResultGraph.ResultGraph
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.model.v2.transducer.TransducerGraph

abstract class AbstractResultGraphBuilder<T: ResultNode, E: ResultEdge>(
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) {

    var maxEdgeCost: Int = 0
    var minEdgeCost: Int = 0
    val unreachableNodesAmount: Int = 0;

    /**
     * construct product graph restricted to nodes (s,t, a) a € Ind(A)
     *
     */
//    abstract fun constructResultGraph(): ResultGraph<T,E>


    /**
     * takes a query and transducer source and target nodes as input.
     * returns a pair of two lists, containing all queryEdges and transducerEdges as follows:
     * queryEdges: all edges from source to target
     * transducerEdges:
     * if requireConceptAssertion=true: all edges (t,u,A?,w,t') s.t. queryEdges contains an edge with label u
     * if requireConceptAssertion=false: all edges (t,u,v,w,t') s.t. queryEdges contains an edge with label u
     * if requireConceptAssertion=null: both of above sets
     */
    fun getCandidateEdges(querySource: Node, queryTarget: Node, transducerSource: Node, transducerTarget: Node, requireConceptAssertion: Boolean?=null): Pair<List<QueryEdge>, List<TransducerEdge>>? {
        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
        if (candidateQueryEdges.isEmpty()) {
            return null;
        }

        var candidateQueryTransitions = candidateQueryEdges.map { it.label }

        //get all edges (t,_,_,_,t1) € TransducerGraph
        var candidateTransducerEdges =
            transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);

        if(requireConceptAssertion == null) {
            candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming))
            }
        }
        else {
            // keep only those that have matching u for some R s.t. (s,u,s1) € query and (t,u,R,w,t1) € trans
            if(requireConceptAssertion) {
                candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                    candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming))
                            && transEdge.label.outgoing.isConceptAssertion();
                }
            }
            else {
                candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                    candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming))
                            && !transEdge.label.outgoing.isConceptAssertion();
                }
            }
        }



        if (candidateTransducerEdges.isEmpty()) {
            return null;
        }

        return Pair(candidateQueryEdges, candidateTransducerEdges)
    }
}