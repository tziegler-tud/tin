package tinDB.services.ontology.ResultGraph

import tinDB.model.v2.ResultGraph.DbResultEdge
import tinDB.model.v2.ResultGraph.DbResultGraph
import tinDB.model.v2.ResultGraph.DbResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph

import tinLIB.model.v2.graph.Node
import tinLIB.services.ontology.ResultGraph.AbstractResultGraphBuilder

class DbResultGraphBuilder(
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,

    ) : AbstractResultGraphBuilder<DbResultNode, DbResultEdge>(queryGraph, transducerGraph) {

    override fun constructRestrictedGraph(): DbResultGraph {
        //TODO: Implement
        return DbResultGraph()
    }

    fun constructResultGraph() : DbResultGraph {
        val resultGraph = constructRestrictedGraph();
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode: Node ->
                queryGraph.nodes.forEach { targetQueryNode ->
                    transducerGraph.nodes.forEach transducerTarget@{ targetTransducerNode: Node ->
//                        ec.forEachIndividual { individual ->
//
//                            //calculate basic classes
//                            val classes = ec.resultGraphReasoner.getClasses(individual);
//                            var minimumCost: Int? = null
//
//                            val candidates: MutableSet<Pair<SingleClassLoopTableEntryRestriction, Int>> = mutableSetOf()
//
//
//                            classes.forEach { owlClassNode ->
//                                val owlClass: OWLClass = owlClassNode.representativeElement;
//                                val restriction = ec.spRestrictionBuilder.createConceptNameRestriction(owlClass)
//                                //get minimal sp value
//                                val entry = ELSPLoopTableEntry(
//                                    Pair(queryNode, transducerNode),
//                                    Pair(targetQueryNode, targetTransducerNode),
//                                    restriction
//                                );
//                                val cost = spTable.get(entry);
//                                if(cost !== null) candidates.add(Pair(restriction, cost));
//                            }
//
//                            if(candidates.isNotEmpty()) {
//                                minimumCost = candidates.minOf { it.second }
//                            }
//
//                            if (minimumCost != null) {
//                                val dlIndividual = individualFactory.fromOWLNamedIndividual(individual)
//                                val sourceNode = DlResultNode(queryNode, transducerNode, dlIndividual );
//                                val targetNode = DlResultNode(targetQueryNode, targetTransducerNode, dlIndividual);
//                                val edge = DlResultEdge(sourceNode, targetNode, minimumCost)
//                                resultGraph.addEdge(edge);
//                            }
//                        }
                    }
                }
            }
        }
        return resultGraph
    }
}