package tin.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLClass
import tin.model.v2.ResultGraph.ResultEdge
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.loopTable.LoopTable.ELH.ELSPLoopTable
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import kotlin.math.min

class ELResultGraphBuilder(
    private val ec: ELExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) : AbstractResultGraphBuilder(ec, queryGraph, transducerGraph) {

    fun constructResultGraph(spTable: ELSPLoopTable) : ResultGraph {
        val resultGraph = constructRestrictedGraph();
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                queryGraph.nodes.forEach { targetQueryNode ->
                    transducerGraph.nodes.forEach transducerTarget@{ targetTransducerNode ->
                        ec.forEachIndividual { individual ->

                            //calculate basic classes
                            val classes = ec.dlReasoner.getClasses(individual);
                            var minimumCost: Int? = null

                            val candidates: MutableSet<Pair<SingleClassLoopTableEntryRestriction, Int>> = mutableSetOf()


                            classes.forEach { owlClassNode ->
                                val owlClass: OWLClass = owlClassNode.representativeElement;
                                val restriction = ec.spRestrictionBuilder.createConceptNameRestriction(owlClass)
                                //get minimal sp value
                                val entry = ELSPLoopTableEntry(
                                    Pair(queryNode, transducerNode),
                                    Pair(targetQueryNode, targetTransducerNode),
                                    restriction
                                );
                                val cost = spTable.get(entry);
                                if(cost !== null) candidates.add(Pair(restriction, cost));
                            }

                            if(candidates.isNotEmpty()) {
                                minimumCost = candidates.minOf { it.second }
                            }

                            if (minimumCost != null) {
                                val sourceNode = ResultNode(queryNode, transducerNode, individual );
                                val targetNode = ResultNode(targetQueryNode, targetTransducerNode, individual);
                                val edge = ResultEdge(sourceNode, targetNode, minimumCost)
                                resultGraph.addEdge(edge);
                            }
                        }
                    }
                }
            }
        }
        return resultGraph
    }
}