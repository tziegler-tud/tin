package tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators

import org.semanticweb.owlapi.model.*
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry

class SpaS1Calculator(
    private val ec: ELExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.spaRestrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;

    /**
     * for each role r given, check if M <= E r. M1
     * returns the set of roles that satisfy the condition
     * Usually, this will be executed over all roles r present in the ontology.
     *
     * Time Complexity is EXP x |roles|.
     *
     */
    fun calculateCandidateRoles(MExp: DLExpression, M1Exp: DLExpression, roles: Set<OWLObjectProperty>) : Set<OWLObjectProperty> {
        //calculate candidate role names r s.t. M <= E r. M1
        var candidateRoles: MutableSet<OWLObjectProperty> = hashSetOf();
        //for each role, check if entailed
        roles.forEach { role ->
            //build class expressions
            val M1ClassExp = M1Exp.getClassExpression();
            val rM1 = expressionBuilder.createExistentialRestriction(role, M1ClassExp)
            val rM1Exp = expressionBuilder.createELHIExpression(rM1);
            //check if entailed
            val isEntailed = dlReasoner.checkIsSubsumed(MExp, rM1Exp)
            if(isEntailed) candidateRoles.add(role);
        }
        return candidateRoles
    }



    /**
     * calculates the updated value for all entries (s,t),(s',t'),C
     */
    fun calculateAll(table: ELSPALoopTable, updateTable: ELSPALoopTable, isInitialIteration: Boolean): ELSPALoopTable {

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction =
            restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)

        val roles = ec.getRoles();

        val newTable = ELSPALoopTable()

        if(!isInitialIteration && updateTable.map.isEmpty()) return newTable;

        var costCutoff: Int? = null;

        queryGraph.nodes.forEach { querySource ->
            transducerGraph.nodes.forEach { transducerSource ->
                queryGraph.nodes.forEach { queryTarget ->
                    transducerGraph.nodes.forEach transducerTarget@{ transducerTarget ->

//                        println("Calculating value for entries of type ( ${querySource.identifier}, ${transducerSource.identifier} ), ( ${queryTarget.identifier}, ${transducerTarget.identifier} ), ___");

                        if (querySource == queryTarget && transducerSource == transducerTarget) {
                            // (s,t),(s,t),M = 0 always
                            //we don't want to add these to the loop table
                            return@transducerTarget
                        }

                        queryGraph.nodes.forEach { candidateQuerySource ->
                            transducerGraph.nodes.forEach candidateTransducerSource@{ candidateTransducerSource ->

                                /**
                                 * Downwards transition
                                 */
//                                get all edges (s,_,s1) € QueryGraph
                                val candidateEdgesDown =
                                    getCandidateEdges(querySource, candidateQuerySource, transducerSource, candidateTransducerSource, costCutoff)
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
                                            getCandidateEdges(candidateQueryTarget, queryTarget, candidateTransducerTarget, transducerTarget, costCutoff)
                                        if(candidateEdgesUp == null) {
                                            return@candidateTransducerTarget
                                        };
                                        val candidateTransducerEdgesUp = candidateEdgesUp.second;
                                        val sortedTransducerEdgesUp = candidateTransducerEdgesUp.sortedBy { it.label.cost }

                                        var candidateResultList: List<Pair<OWLClassExpression, Int>> = mutableListOf();

                                        //only add (s,t,)(s,t),0 candidates for initial iteration (cannot be improved later on)
                                        if(isInitialIteration && candidateQuerySource == candidateQueryTarget && candidateTransducerSource == candidateTransducerTarget) {
                                            val mutableList: MutableList<Pair<OWLClassExpression, Int>> = mutableListOf()
                                            ec.forEachConcept { owlClass ->
                                                mutableList.add(Pair(restrictionBuilder.asClassExpression(owlClass), 0))
                                            }
                                            candidateResultList = mutableList;
                                        }
                                        else {
                                            val candidateMap = updateTable.getWithSourceAndTarget(
                                                Pair(candidateQuerySource, candidateTransducerSource), Pair(candidateQueryTarget, candidateTransducerTarget), costCutoff).map.toMutableMap()

                                            if(candidateMap.isEmpty()) return@candidateTransducerTarget

                                            //create optimized map to use from here. We only need class expression and value
                                            candidateResultList = candidateMap.map { (entry, v) ->
                                                Pair(restrictionBuilder.asClassExpression(entry.restriction), v)
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

                                        //we have a set of candidate R associated with the best possible pair of trans edges
                                        //now, we have to check if C <= €rA for some R in candidateEdgesMap
                                        //otherwise, reject candidate entry
                                        candidateEdgeMap.forEach candidateEdges@ { (role, pairOfEdges) ->
                                            val edgeCost: Int = pairOfEdges.first.label.cost + pairOfEdges.second.label.cost;

                                            candidateResultList.forEach candidates@{ candidateResultPair ->
                                                //build class expressions
                                                val M1ClassExp = candidateResultPair.first;
                                                val candidateCost = candidateResultPair.second;
                                                val rM1 = expressionBuilder.createExistentialRestriction(role, M1ClassExp)
                                                val rM1Exp = expressionBuilder.createELHIExpression(rM1);

                                                //calculate basic classes A that satisfy A <= €R.C
                                                val atomicSubsumers = dlReasoner.calculateSubClasses(rM1Exp)
                                                atomicSubsumers.forEach{  owlClass->
                                                    val entry = ELSPALoopTableEntry(
                                                        querySource,
                                                        transducerSource,
                                                        queryTarget,
                                                        transducerTarget,
                                                        restrictionBuilder.createConceptNameRestriction(owlClass)
                                                    )
                                                    val result: Int = edgeCost + candidateCost
                                                    newTable.setIfLower(entry, result);
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
        return newTable;
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