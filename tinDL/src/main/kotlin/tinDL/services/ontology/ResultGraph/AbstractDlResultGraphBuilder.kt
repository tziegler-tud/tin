package tinDL.services.ontology.ResultGraph

import tinDL.model.v2.ResultGraph.*

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinLIB.model.v2.graph.Node
import tinLIB.services.ontology.ResultGraph.AbstractResultGraphBuilder
import tinLIB.services.ontology.ResultGraph.ResultGraphBuilderStats

abstract class AbstractDlResultGraphBuilder(
    private val ec: ExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) : AbstractResultGraphBuilder<DlResultNode, DlResultEdge>(queryGraph, transducerGraph) {

    val individualFactory = DlResultGraphIndividualFactory(ec.shortFormProvider)


    fun getStats(resultGraph: DlResultGraph) : ResultGraphBuilderStats {
        return ResultGraphBuilderStats(
            resultGraph.nodes.size,
            resultGraph.edges.size,
            resultGraph.edges.maxOf { it.label.cost },
            resultGraph.edges.minOf { it.label.cost },
            findUnreachableNodes(resultGraph).size
        )
    }

    private fun findUnreachableNodes(resultGraph: DlResultGraph) : List<DlResultNode> {
        var counter: Int = 0;
        val unreachableNodes: MutableList<DlResultNode> = mutableListOf()
        val nodesQueue: MutableList<DlResultNode> = resultGraph.nodes.asList().toMutableList();
        //a node is unreachable if it has no incoming edges from a node different from itself.
        if(nodesQueue.size <= 1) return unreachableNodes;
        nodesQueue.forEach { node ->
            val edges = resultGraph.getEdgesWithTarget(node);
            if(edges.isNotEmpty()) return@forEach;
            if(edges.filter{it.source !== node}.isNotEmpty()) return@forEach;
            unreachableNodes.add(node);
        }
        return unreachableNodes;
    }

    open fun constructRestrictedGraph(): DlResultGraph {
        val graph = DlResultGraph(ec.shortFormProvider);
        //construct nodes
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode: Node ->

                ec.forEachIndividual { individual ->
                    val dlIndividual = individualFactory.fromOWLNamedIndividual(individual)
                    val node = DlResultNode(queryNode, transducerNode, dlIndividual)
                    graph.addNode(node)

                    queryGraph.nodes.forEach { targetQueryNode ->
                        transducerGraph.nodes.forEach transducerTarget@ { targetTransducerNode: Node ->

                            var targetNodeSelf = DlResultNode(targetQueryNode, targetTransducerNode, dlIndividual);
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
                                            graph.addEdge(DlResultEdge(node, targetNodeSelf, transEdge.label.cost))
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
                                    val dlTargetIndividual = individualFactory.fromOWLNamedIndividual(targetIndividual)
                                    val targetNode = DlResultNode(targetQueryNode, targetTransducerNode, dlTargetIndividual);
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
}