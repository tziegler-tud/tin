package tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators

import org.semanticweb.owlapi.model.*
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPLoopTable
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import tinDL.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry

class SpCalculator(
    private val ec: ELExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val shortFormProvider = ec.shortFormProvider;
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.spaRestrictionBuilder;
    private val spRestrictionBuilder = ec.spRestrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;

    /**
     * calculates the final value for all entries (s,t),(s',t'),a
     */
    fun calculateAll(spaTable: ELSPALoopTable): ELSPLoopTable {

        val roles = ec.getRoles();
        val table = ELSPLoopTable()

        queryGraph.nodes.forEach { querySource ->
            transducerGraph.nodes.forEach { transducerSource ->
                queryGraph.nodes.forEach { queryTarget ->
                    transducerGraph.nodes.forEach transducerTarget@{ transducerTarget ->


                        queryGraph.nodes.forEach { candidateQuerySource ->
                            transducerGraph.nodes.forEach candidateTransducerSource@{ candidateTransducerSource ->

                                /**
                                 * Downwards transition
                                 */
//                                get all edges (s,_,s1) € QueryGraph
                                val candidateEdgesDown =
                                    getCandidateEdges(querySource, candidateQuerySource, transducerSource, candidateTransducerSource, null)

                                if (candidateEdgesDown == null) {
                                    return@candidateTransducerSource
                                };
                                val candidateTransducerEdgesDown = candidateEdgesDown.second;
                                val sortedTransducerEdgesDown = candidateTransducerEdgesDown.sortedBy { it.label.cost }

                                queryGraph.nodes.forEach { candidateQueryTarget ->
                                    transducerGraph.nodes.forEach candidateTransducerTarget@{ candidateTransducerTarget ->

                                        /**
                                         * Upwards transition
                                         */
                                        val candidateEdgesUp =
                                            getCandidateEdges(candidateQueryTarget, queryTarget, candidateTransducerTarget, transducerTarget, null)
                                        if(candidateEdgesUp == null) {
                                            return@candidateTransducerTarget
                                        };
                                        val candidateTransducerEdgesUp = candidateEdgesUp.second;
                                        val sortedTransducerEdgesUp = candidateTransducerEdgesUp.sortedBy { it.label.cost }

                                        var candidateResultList: List<Pair<SingleClassLoopTableEntryRestriction, Int>>


                                        if(candidateQuerySource == candidateQueryTarget && candidateTransducerSource == candidateTransducerTarget) {
                                            val mutableList: MutableList<Pair<SingleClassLoopTableEntryRestriction, Int>> = mutableListOf()
                                            ec.forEachConcept { owlClass ->
                                                mutableList.add(Pair(owlClass, 0))
                                            }
                                            candidateResultList = mutableList;
                                        }
                                        else {
                                            val candidateMap = spaTable.getWithSourceAndTarget(
                                                Pair(candidateQuerySource, candidateTransducerSource), Pair(candidateQueryTarget, candidateTransducerTarget), null).map
                                            if(candidateMap.isEmpty()) return@candidateTransducerTarget
                                            //create optimized map to use from here. We only need class expression and value
                                            candidateResultList = candidateMap.map { (entry, v) ->
                                                Pair(entry.restriction, v)
                                            }.sortedBy{it.second}
                                        }

                                        if(candidateResultList.isEmpty()) return@candidateTransducerTarget

                                        //find valid role names R s.t. R <= R' and R- <= R''
                                        //for each R, we only keep the best R' and R''
                                        val candidateEdgeMap: MutableMap<OWLObjectProperty, Pair<TransducerEdge, TransducerEdge>> = mutableMapOf()

                                        roles.forEach roles@ { role ->
                                            var downEdge: TransducerEdge? = null;
                                            var upEdge: TransducerEdge? = null;
                                            run downIterator@ {
                                                sortedTransducerEdgesDown.forEach down@{ down ->
                                                    val outgoingLabel = down.label.outgoing;
                                                    if(outgoingLabel.isInverse()) return@down;
                                                    val downProperty =
                                                        queryParser.getOWLObjectPropertyExpression(down.label.outgoing)
                                                            ?: return@down
                                                    val downEntailed =
                                                        dlReasoner.checkPropertySubsumption(role, downProperty);
                                                    if (downEntailed) {
                                                        downEdge = down;
                                                        // we could exit the for loop at this point because the set was ordered
                                                        return@downIterator
                                                    }
                                                }
                                            }


                                            if (downEdge == null) return@roles;

                                            run upIterator@ {
                                                sortedTransducerEdgesUp.forEach up@{ up ->
                                                    //CAREFUL FOR ELH: This is required to be an inverse role
                                                    val outgoingLabel = up.label.outgoing;
                                                    if(!outgoingLabel.isInverse()) return@up;

                                                    val nonInverseProperty = outgoingLabel.getInverseAsNewProperty()

                                                    val upProperty =
                                                        queryParser.getOWLObjectPropertyExpression(nonInverseProperty)
                                                            ?: return@up

                                                    val upEntailed = dlReasoner.checkPropertySubsumption(
                                                        role,
                                                        upProperty
                                                    )
                                                    if (upEntailed) {
                                                        upEdge = up;
                                                        return@upIterator
                                                    }
                                                }
                                            }

                                            if (upEdge == null) return@roles;
                                            candidateEdgeMap[role] = Pair(downEdge!!, upEdge!!);
                                        }

                                        if(candidateEdgeMap.isEmpty()) return@candidateTransducerTarget

                                        //for each concept name C
                                        //we have a set of candidate R associated with the best possible pair of trans edges
                                        //now, we have to check if C <= €rA for some R in candidateEdgesMap
                                        //otherwise, reject candidate entry
                                        candidateEdgeMap.forEach candidateEdges@ { (role, pairOfEdges) ->
                                            val edgeCost: Int = pairOfEdges.first.label.cost + pairOfEdges.second.label.cost;

                                            candidateResultList.forEach candidates@{ candidateResultPair ->
                                                //build class expressions
                                                val M1ClassExp = restrictionBuilder.asClassExpression(candidateResultPair.first);
                                                val candidateCost = candidateResultPair.second;
                                                val rM1 = expressionBuilder.createExistentialRestriction(role, M1ClassExp)
                                                val rM1Exp = expressionBuilder.createELHIExpression(rM1);

                                                //calculate basic classes A that satisfy A <= €R.C
                                                val atomicSubsumers = dlReasoner.calculateSubClasses(rM1Exp)
                                                //if there are no atomic subsumers, exit and try next candidate
                                                if (atomicSubsumers.isEmpty()) {
                                                    return@candidates;
                                                }

                                                ec.forEachConcept tailsets@{  tailRestriction ->

                                                    if(!tailRestriction.isContainedInSet(atomicSubsumers)) {
                                                        return@tailsets;
                                                    }

                                                    val entry = ELSPLoopTableEntry(
                                                        querySource,
                                                        transducerSource,
                                                        queryTarget,
                                                        transducerTarget,
                                                        tailRestriction
                                                    )
                                                    val result: Int = edgeCost + candidateCost
                                                    table.setIfLower(entry, result);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return table;
    }

    private fun getCandidateEdges(querySource: Node, queryTarget: Node, transducerSource: Node, transducerTarget: Node, maxCost: Int?): Pair<List<QueryEdge>, List<TransducerEdge>>? {
        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
        if (candidateQueryEdges.isEmpty()) {
            return null;
        }

        var candidateQueryTransitions = candidateQueryEdges.map { it.label }

        //get all edges (t,_,_,_,t1) € TransducerGraph
        var candidateTransducerEdges =
            transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);
        // keep only those that have matching u for some R s.t. (s,u,s1) € query and (t,u,R,w,t1) € trans and w < costCutoff
        if(maxCost == null) {
            candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming)) &&
                        !transEdge.label.outgoing.isConceptAssertion();
            }
        }
        else {
            candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming)) &&
                        !transEdge.label.outgoing.isConceptAssertion() &&
                        transEdge.label.cost <= maxCost;
            }
        }

        if (candidateTransducerEdges.isEmpty()) {
            return null;
        }

        return Pair(candidateQueryEdges, candidateTransducerEdges)
    }


}