package tinDL.services.ontology.ResultGraph

import tinDL.model.v2.ResultGraph.ResultEdge
import tinDL.model.v2.ResultGraph.ResultGraph
import tinDL.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

open class AbstractResultGraphBuilder(
    private val ec: ExecutionContext,
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
    fun constructRestrictedGraph(): ResultGraph {
        val graph = ResultGraph();
        //construct nodes
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->

                ec.forEachIndividual { individual ->
                    val node = ResultNode(queryNode, transducerNode, individual);
                    graph.addNode(node)

                    queryGraph.nodes.forEach { targetQueryNode ->
                        transducerGraph.nodes.forEach transducerTarget@ { targetTransducerNode ->

                            var targetNodeSelf = ResultNode(targetQueryNode, targetTransducerNode, individual);
                            graph.addNode(targetNodeSelf)

                            //find edges where ind = ind and v = A?
                            val candidateEdgesSelf = getCandidateEdges(
                                queryNode,
                                targetQueryNode,
                                transducerNode,
                                targetTransducerNode,
                                true
                            );

                            if (candidateEdgesSelf != null) {
                                val transducerEdges = candidateEdgesSelf.second
                                if(transducerEdges.isNotEmpty()) {

                                    //calc basic classes of element
                                    val classes = ec.resultGraphReasoner.getClasses(individual);
                                   transducerEdges.forEach transEdges@ { transEdge ->
                                       val transLabel = transEdge.label.outgoing;
                                       if(!transLabel.isConceptAssertion()) return@transEdges //smt went wrong

                                       val assertedClass = ec.parser.getOWLClass(transLabel) ?: return@transEdges;

                                       if(classes.containsEntity(assertedClass)) {
                                           graph.addEdge(ResultEdge(node, targetNodeSelf, transEdge.label.cost))
                                       }
                                   }
                                }
                            }

                            val candidateEdges = getCandidateEdges(
                                queryNode,
                                targetQueryNode,
                                transducerNode,
                                targetTransducerNode,
                                false
                            );
                            if(candidateEdges == null || candidateEdges.second.isEmpty()) {
                                return@transducerTarget
                            }

                            for (transducerEdge in candidateEdges.second) {
                                val role = transducerEdge.label.outgoing;
                                if(role.isConceptAssertion()) continue;
                                val propertyExpression = ec.parser.getOWLObjectPropertyExpression(role) ?: continue;
                                val connectedIndividuals = ec.resultGraphReasoner.getConnectedIndividuals(propertyExpression, individual)

                                for (individualNode in connectedIndividuals) {
                                    val targetIndividual = individualNode.representativeElement
                                    val targetNode = ResultNode(targetQueryNode, targetTransducerNode, targetIndividual);
                                    graph.addNode(targetNode)
                                    graph.addEdge(node, targetNode, transducerEdge.label.cost);
                                }
                            }
                        }
                    }
                }
            }
        }
        return graph;
    }

    private fun getCandidateEdges(querySource: Node, queryTarget: Node, transducerSource: Node, transducerTarget: Node, requireConceptAssertion: Boolean): Pair<List<QueryEdge>, List<TransducerEdge>>? {
        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
        if (candidateQueryEdges.isEmpty()) {
            return null;
        }

        var candidateQueryTransitions = candidateQueryEdges.map { it.label }

        //get all edges (t,_,_,_,t1) € TransducerGraph
        var candidateTransducerEdges =
            transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);

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


        if (candidateTransducerEdges.isEmpty()) {
            return null;
        }

        return Pair(candidateQueryEdges, candidateTransducerEdges)
    }
}