package tinDL.services.ontology.ResultGraph

import org.semanticweb.owlapi.model.OWLClass
import tinDL.model.v2.ResultGraph.ResultEdge
import tinDL.model.v2.ResultGraph.ResultGraph
import tinDL.model.v2.ResultGraph.ResultNode
import tinDL.model.v2.graph.Node
import tinDL.model.v2.query.QueryEdge
import tinDL.model.v2.query.QueryEdgeLabel
import tinDL.model.v2.query.QueryGraph
import tinDL.model.v2.transducer.TransducerEdge
import tinDL.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPLoopTable
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassConceptNameRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
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
                            val classes = ec.resultGraphReasoner.getClasses(individual);
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