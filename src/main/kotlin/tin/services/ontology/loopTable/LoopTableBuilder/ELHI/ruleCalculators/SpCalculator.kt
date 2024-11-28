package tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators

import org.semanticweb.owlapi.model.*
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry


class SpCalculator(
    private val ec: ELHIExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val shortFormProvider = ec.shortFormProvider;
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.spaRestrictionBuilder;
    private val spRestrictionBuilder = ec.spRestrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;
    private val manchesterShortFormProvider = ec.manchesterShortFormProvider;

    /**
     * calculates the final value for all entries (s,t),(s',t'),a
     */
    fun calculateAll(spaTable: ELHISPALoopTable): ELHISPLoopTable {

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction =
            restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)

        val roles = ec.getRoles();
        val spTable = ELHISPLoopTable()

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

                                        var candidateResultList: List<Pair<MultiClassLoopTableEntryRestriction, Int>>


                                        if(candidateQuerySource == candidateQueryTarget && candidateTransducerSource == candidateTransducerTarget) {
                                            candidateResultList = mutableListOf(Pair(owlTopClassRestriction, 0))
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
                                                    val upProperty =
                                                        queryParser.getOWLObjectPropertyExpression(up.label.outgoing)
                                                            ?: return@up
                                                    val upEntailed = dlReasoner.checkPropertySubsumption(
                                                        role.inverseProperty,
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

                                        //sort candidateResultList by length of restriction DESCENDING ( == from most specific to less specific)
                                        var sortedCandidateResultList = candidateResultList.sortedBy { pair ->
                                            pair.first.getSize()
                                        }

                                        //for each a € Ind(A), we have to find the best combination of M0 and M s.t.
                                        //M0(a) is entailed, M0 c E R.M is entailed
                                        ec.forEachIndividual individuals@ { individual ->

                                            val classes = dlReasoner.getClasses(individual)
                                            val M0Exp = expressionBuilder.createELHIExpression(classes);

                                            var minResult: Int? = null;
                                            //we have a set of candidate R associated with the best possible pair of trans edges
                                            //now, we have to check if M0 <= €rM1 for some R in candidateEdgesMap
                                            //otherwise, reject candidate entry
                                            candidateEdgeMap.forEach candidateEdges@ { (role, pairOfEdges) ->
                                                val eliminatedSets: MutableSet<MultiClassLoopTableEntryRestriction> = mutableSetOf();
                                                val resultSets: MutableSet<MultiClassLoopTableEntryRestriction> = mutableSetOf();
                                                val resultMap: MutableMap<MultiClassLoopTableEntryRestriction, Int> = mutableMapOf();

                                                var csCounter = 0;
                                                val edgeCost: Int = pairOfEdges.first.label.cost + pairOfEdges.second.label.cost;


                                                //start with the most specific candidateSet.
                                                sortedCandidateResultList.forEach candidates@{ candidateResultPair ->

                                                    //build class expressions
                                                    val M1Restriction = candidateResultPair.first

                                                    if(M1Restriction.containsAllElementsFromOneOf(eliminatedSets)) {
                                                        return@candidates;
                                                    }

                                                    if(M1Restriction.containsOnlyElementsFromOneOf(resultSets)) {
                                                        return@candidates;
                                                    }

                                                    val M1ClassExp =
                                                        restrictionBuilder.asClassExpression(M1Restriction);
                                                    val candidateCost = candidateResultPair.second;
                                                    val rM1 =
                                                        expressionBuilder.createExistentialRestriction(role, M1ClassExp)
                                                    val rM1Exp = expressionBuilder.createELHIExpression(rM1);

                                                    val isEntailed = dlReasoner.checkIsSubsumed(M0Exp, rM1Exp)

                                                    if(!isEntailed) {
                                                        eliminatedSets.add(M1Restriction)
                                                    }
                                                    else {
                                                        val result = edgeCost + candidateCost
                                                        if(minResult == null || result < minResult!!) minResult = result;
                                                        resultSets.add(M1Restriction);
                                                        resultMap[M1Restriction] = candidateCost;
                                                    }
                                                }

                                            }

                                            //update entry for (s,s'),(t,t'),a if cost is not null (=+infinity)
                                            if(minResult != null) {
                                                val individualRestriction = spRestrictionBuilder.createNamedIndividualRestriction(individual)
                                                val spEntry = IndividualLoopTableEntry(querySource, transducerSource, queryTarget, transducerTarget, individualRestriction)
                                                spTable.setIfLower(spEntry, minResult!!);
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
        return spTable;
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