package tin.services.ontology.loopTable.LoopTableBuilder.old.okt1824.ruleCalculators.v2

import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.DLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.ConceptNameRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import java.util.*
import kotlin.math.min

class SpaS1Calculator(
    private val ec: OntologyExecutionContext,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph
    ) {

    private val shortFormProvider = ec.shortFormProvider;
    private val queryParser = ec.parser;
    private val restrictionBuilder = ec.restrictionBuilder;
    private val expressionBuilder = ec.expressionBuilder;
    private val dlReasoner = ec.dlReasoner;
    private val manchesterShortFormProvider = ec.manchesterShortFormProvider;

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
     * calculates the updated value for all entries (s,t),(s',t'),M
     */
    fun calculateAllV2(table: SPALoopTable, updateTable: SPALoopTable, isInitialIteration: Boolean = true): SPALoopTable {

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction =
            restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)

        val roles = ec.getRoles();
        val roleNames = ec.getRoleNames();

        val newTable = SPALoopTable()

        if(updateTable.map.isEmpty()) return newTable;

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
                                val candidateQueryEdgesDown = candidateEdgesDown.first;
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
                                        val candidateQueryEdgesUp = candidateEdgesUp.first;
                                        val candidateTransducerEdgesUp = candidateEdgesUp.second;
                                        val sortedTransducerEdgesUp = candidateTransducerEdgesUp.sortedBy { it.label.cost }

                                        var candidateResultList: List<Pair<OWLClassExpression, Int>> = mutableListOf();

                                        //only add (s,t,)(s,t),0 candidates for initial iteration (cannot be improved later on)
                                        if(isInitialIteration && candidateQuerySource == candidateQueryTarget && candidateTransducerSource == candidateTransducerTarget) {
                                            val mutableList: MutableList<Pair<OWLClassExpression, Int>> = mutableListOf()
                                            ec.tailsetsAsClasses.forEach { tailset ->
                                                mutableList.add(Pair(expressionBuilder.createClassExpression(tailset), 0))
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

                                        //we have a set of candidate R associated with the best possible pair of trans edges
                                        //now, we have to check if M <= €rM1 for some R in candidateEdgesMap
                                        //otherwise, reject candidate entry
                                        val resultList: MutableMap<OWLObjectProperty, Int> = mutableMapOf();
                                        candidateEdgeMap.forEach candidateEdges@ { (role, pairOfEdges) ->
                                            var tcCounter = 0;
                                            val edgeCost: Int = pairOfEdges.first.label.cost + pairOfEdges.second.label.cost;

                                            candidateResultList.forEach candidates@{ candidateResultPair ->
                                                //build class expressions
                                                val M1ClassExp = candidateResultPair.first;
                                                val candidateCost = candidateResultPair.second;
                                                val rM1 = expressionBuilder.createExistentialRestriction(role, M1ClassExp)
                                                val rM1Exp = expressionBuilder.createELHIExpression(rM1);

                                                /**
                                                 * debug line
                                                 */
                                                tcCounter++;
//                                                println("Calculating candidate set " + tcCounter + "/ " + candidateResultList.size);
//                                                if(tcCounter == 10) return@candidateEdges

                                                val eliminatedSets: HashSet<ConceptNameRestriction> = hashSetOf();

                                                /**
                                                 * Test: build restriction consisting of all concept names
                                                 */
                                                val allClasses: ConceptNameRestriction = ConceptNameRestriction();
                                                ec.getClasses().forEach { owlClass ->
                                                    allClasses.addElement(owlClass.asOWLClass());
                                                }

                                                val allClassExpr = expressionBuilder.createELHIExpression(restrictionBuilder.asClassExpression(allClasses))
                                                val isEntailed = dlReasoner.checkIsSubsumed(allClassExpr, rM1Exp)
                                                if(!isEntailed) {
                                                    eliminatedSets.add(allClasses)
                                                };

                                                if(eliminatedSets.isNotEmpty()) return@candidates;

                                                ec.tailsetsAsClasses.forEach tailsets@{ tailset ->

                                                    val restriction =
                                                        restrictionBuilder.createConceptNameRestriction(tailset)

                                                    val entry = SPALoopTableEntry(
                                                        querySource,
                                                        transducerSource,
                                                        queryTarget,
                                                        transducerTarget,
                                                        restriction
                                                    )


                                                    val costCutoff = table.get(entry)
                                                    //check if the added weight of transducer edges is already higher than the current entry
                                                    if (costCutoff !== null && costCutoff <= edgeCost) return@tailsets; //we cannot improve an entry with cost 0
                                                    //0, int val or null

                                                    //list contains matching candidates for the chosen M. We only have to confirm that M <= €rM1
                                                    // list is ordered by value, meaning that once we find a suitable candidate we can not improve anymore

                                                    val result: Int = edgeCost + candidateCost

                                                    //calculate potential result
                                                    /**
                                                     * handle predictable entries separately
                                                     */
//                                                  if(candidate.key.hasEqualSourceAndTarget()) return@candidates

                                                    /**
                                                     * performance-optimized insert operation.
                                                     * This looks strange, but is a little faster than the obvious way to implement this
                                                     */
                                                    if (costCutoff == null || costCutoff > result) {

                                                        //if M is a subset of an element in eliminatedSets, it cannot be entailed
                                                        eliminatedSets.forEach { set ->
                                                            if(restriction.isSubsetOf(set)) {
                                                                return@tailsets
                                                            }
                                                        }

                                                        val MClassExp =
                                                            restrictionBuilder.asClassExpression(restriction);
                                                        val MExp = expressionBuilder.createELHIExpression(MClassExp);

                                                        //check if entailed
                                                        val isEntailed = dlReasoner.checkIsSubsumed(MExp, rM1Exp)
//                                                        val isEntailed = false
                                                        if (isEntailed) {
                                                            //everything in place, this is valid rule application
                                                            //update entry with final value
                                                            newTable.set(entry, result);
                                                            return@tailsets
                                                        }
                                                        else {
                                                            eliminatedSets.add(restriction);
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