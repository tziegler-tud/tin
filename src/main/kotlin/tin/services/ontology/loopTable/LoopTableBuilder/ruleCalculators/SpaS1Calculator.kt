package tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators

import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLPropertyExpression
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
     * calculates the updated value for an entry spa[(s,t),(s',t'),M]
     */
    fun calculate(spaLoopTableEntry: SPALoopTableEntry, table: SPALoopTable): Int? {
        val source = spaLoopTableEntry.source;
        val target = spaLoopTableEntry.target;
        val M = spaLoopTableEntry.restriction;
        val s = source.first;
        val t = source.second;
        val se = target.first;
        val te = target.second;

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction = restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)

        val costCutoff = table.get(spaLoopTableEntry) //0, int val or null

        if (costCutoff == 0) return 0; //we cannot improve an entry with cost 0

        val MCLassExp = restrictionBuilder.asClassExpression(M);
        val MExp = expressionBuilder.createELHIExpression(MCLassExp);

        //list all entries < costCutoff
        //these are the non-trivial candidate entries spa[(s1,t1),(s2,t2),M1] that need to be checked
        val candidateMap = table.getWithCostLimit(costCutoff).toMutableMap()

        //add one trivial entry for each s1,t1. M1 is arbitrary because spa[(s1,t1),(s1,t1),M1] = 0 for all M1
//        queryGraph.nodes.forEach { queryNode ->
//            transducerGraph.nodes.forEach { transducerNode ->
//                candidateMap[SPALoopTableEntry(Pair(queryNode,transducerNode), Pair(queryNode,transducerNode), owlTopClassRestriction)] =
//                    0
//            }
//        }

        var candidateRolesFromTable: Set<OWLObjectProperty>
        var candidateRolesSym: Set<OWLObjectProperty>;

        var minimumCostCandidate : SPALoopTableEntry? = null;
        var minimumCost: Int = -1;

        val restrictions = candidateMap.map {
            it.key.restriction
        }.distinct()

        //iterate through candidates and perform steps 2.1 - 2.6
        candidateMap.forEach { (candidateEntry, candidateCost) ->
            val s1: Node = candidateEntry.source.first;
            val s2 = candidateEntry.target.first;
            val t1 = candidateEntry.source.second;
            val t2 = candidateEntry.target.second;
            var M1 = candidateEntry.restriction;

            val roleNames = ec.getRoleNames();
            val roles = ec.getRoles();

            val M1ClassExp = restrictionBuilder.asClassExpression(M1);
            val M1Exp = expressionBuilder.createELHIExpression(M1ClassExp);

            // calculate candidate roles that satisfy M <= E r. M1
            //complexity is EXP x |roles in ontology|
            candidateRolesFromTable = calculateCandidateRoles(MExp, M1Exp, roles);

            candidateRolesFromTable.forEach lit@ { role ->

                //for each r, calculate superclass r'
                val superR = dlReasoner.calculateSuperProperties(role);
//                val superR = dlReasoner.calculateSuperProperties(role);
                //we need to add the role itself, as it is not included in the superroles calculated by the reasoner



                /**
                 * Downwards transition
                 */
                //get all edges (s,_,s1) € QueryGraph
                var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(s,s1);
                if(candidateQueryEdges.isEmpty()) {
                    return@lit;
                }
                var candidateQueryTransitions = candidateQueryEdges.map{it.label}

                //get all edges (t,_,_,_,t1) € TransducerGraph
                var candidateTransducerEdgesDown = transducerGraph.getEdgesWithSourceAndTarget(t, t1);
                if(candidateTransducerEdgesDown.isEmpty()) {
                    return@lit;
                }
                val sortedTransducerEdgesDown = candidateTransducerEdgesDown.sortedBy { it.label.cost }

                //find the minimal cost of an edge (t,u,R',w,t1) with:
                //  u in candidateQueryTransitions
                //  R' in superR

                var minCostEdgeDown: TransducerEdge? = null;
                val validEdges: MutableList<TransducerEdge> = mutableListOf()
                sortedTransducerEdgesDown.forEach edgeCheck@ { transducerEdge ->
                    val inLabel = transducerEdge.label.incoming;
                    val outLabel = transducerEdge.label.outgoing;

                    //outgoing label must be a role name, not a concept assertion;
                    if(outLabel.isConceptAssertion()) return@edgeCheck;
                    // try to match edge label to positive role name
                    var edgeProperty: OWLObjectPropertyExpression = queryParser.getOWLObjectPropertyExpression(outLabel) ?: return@edgeCheck;
                    //if no property could be obtained, there is no way to continue - this also means our transducer uses property names which are not part of our ontology.

                    val isValidQueryTransition = candidateQueryTransitions.contains(QueryEdgeLabel(inLabel))
                            && (superR.containsEntity(edgeProperty) || edgeProperty == role);
                    if(isValidQueryTransition) {
                        validEdges.add(transducerEdge)
                        if (minCostEdgeDown == null || minCostEdgeDown!!.label.cost > transducerEdge.label.cost) {
                            minCostEdgeDown = transducerEdge;
                        }
                    };
                }

                if(minCostEdgeDown == null) {
                    return@lit;
                }

                /**
                 * upwards transition
                 */
                //get all edges (s2,u',s') € QueryGraph
                var candidateQueryEdgesUp = queryGraph.getEdgesWithSourceAndTarget(s2,se);
                if(candidateQueryEdgesUp.isEmpty()) {
                    return@lit;
                }
                var candidateQueryTransitionsUp = candidateQueryEdgesUp.map{it.label}
                var candidateTransducerEdgesUp = transducerGraph.getEdgesWithSourceAndTarget(t2, te);
                if(candidateTransducerEdgesUp.isEmpty()) {
                    return@lit;
                }

                //now, we have to go back up with an inverse role from superR.
                val sortedTransducerEdgesUp = candidateTransducerEdgesUp.sortedBy { it.label.cost }
                var minCostEdgeUp: TransducerEdge? = null;
                val validEdgesUp: MutableList<TransducerEdge> = mutableListOf()
                sortedTransducerEdgesUp.forEach edgeCheck@ { transducerEdge ->
                    val inLabel = transducerEdge.label.incoming;
                    val outLabel = transducerEdge.label.outgoing;

                    //outgoing label must be a role name, not a concept assertion;
                    if(outLabel.isConceptAssertion()) return@edgeCheck;
                    // try to match edge label to positive role name
                    val edgeProperty: OWLObjectPropertyExpression = queryParser.getOWLObjectPropertyExpression(outLabel) ?: return@edgeCheck;
                    //if no property could be obtained, there is no way to continue - this also means our transducer uses property names which are not part of our ontology.
                    val inverseEdgeProperty = edgeProperty.inverseProperty;

                    val isValidQueryTransition = candidateQueryTransitionsUp.contains(QueryEdgeLabel(inLabel))
                            && (superR.containsEntity(inverseEdgeProperty) || inverseEdgeProperty == role);

                    if(isValidQueryTransition) {
                        validEdgesUp.add(transducerEdge)
                        if (minCostEdgeUp == null || minCostEdgeUp!!.label.cost > transducerEdge.label.cost) {
                            minCostEdgeUp = transducerEdge;
                        }
                    };

                }
                if(minCostEdgeUp == null) {
                    return@lit;
                }

                val localCost = minCostEdgeDown!!.label.cost + candidateCost + minCostEdgeUp!!.label.cost
                if (localCost < minimumCost || minimumCost == -1){
                    minimumCost = localCost;
                    minimumCostCandidate = candidateEntry;
                }
            }
        }

        if(minimumCost == -1) {
            //we could not find an updated value that was smaller than the current value (cost cutoff)
            return costCutoff;
        }
        else {
            return minimumCost;
        }

//        queryGraph.nodes.forEach {
//            transducerGraph.nodes.forEach { transducerNode ->
//                tailsets.forEach { tailset ->
//                    candidateRolesSym
//                }
//            }
//        }
    }

    /**
     * calculates the updated value for all entries (s,t),(s',t'),M
     */
    fun calculateAll(table: SPALoopTable): SPALoopTable {

        val topClassNode = dlReasoner.getTopClassNode();
        val owlTopClassRestriction =
            restrictionBuilder.createConceptNameRestriction(topClassNode.representativeElement)

        val roles = ec.getRoles();
        val roleNames = ec.getRoleNames();

        var costCutoff: Int? = null;

        queryGraph.nodes.forEach { querySource ->
            transducerGraph.nodes.forEach { transducerSource ->
                queryGraph.nodes.forEach { queryTarget ->
                    transducerGraph.nodes.forEach transducerTarget@{ transducerTarget ->

                        println("Calculating value for entries of type ( ${querySource.identifier}, ${transducerSource.identifier} ), ( ${queryTarget.identifier}, ${transducerTarget.identifier} ), ___");

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


                                        val candidateMap = table.getWithSourceAndTarget(
                                            Pair(candidateQuerySource, candidateTransducerSource), Pair(candidateQueryTarget, candidateTransducerTarget), costCutoff).map.toMutableMap()
                                        if(candidateMap.isEmpty()) return@candidateTransducerTarget

                                        //create optimized map to use from here. We only need class expression and value
                                        val candidateResultList = candidateMap.map { (entry, v) ->
                                            val candidateRestriction = entry.restriction;
                                            //build class expressions
                                            val M1ClassExp = restrictionBuilder.asClassExpression(candidateRestriction);
                                            Pair(M1ClassExp, v)
                                        }

                                        //find valid role names R s.t. R <= R' and R- <= R''
                                        //for each R, we only keep the best R' and R''
                                        val candidateEdgeMap: MutableMap<OWLObjectProperty, Pair<TransducerEdge, TransducerEdge>> = mutableMapOf()

                                        roles.forEach roles@ { role ->
                                            sortedTransducerEdgesDown.forEach down@ { down ->
                                                val downProperty =
                                                    queryParser.getOWLObjectPropertyExpression(down.label.outgoing) ?: return@roles
                                                val downEntailed = dlReasoner.checkPropertySubsumption(role, downProperty);
                                                if(!downEntailed) return@down

                                                sortedTransducerEdgesUp.forEach up@{ up ->
                                                    //eliminate pair if combined cost is > costCutoff
//                                                    if(remainingCost != null) {
//                                                        if(down.label.cost + up.label.cost > remainingCost) return@up;
//                                                    }
                                                    /**
                                                     * debug line
                                                     */
                                                    if(up.label.outgoing.toString() == "inverse(contains)") {
                                                        println("debug!")
                                                    }

                                                    val upProperty =
                                                        queryParser.getOWLObjectPropertyExpression(up.label.outgoing) ?: return@roles
                                                    val upEntailed = dlReasoner.checkPropertySubsumption(role.inverseProperty, upProperty)
                                                    if(!upEntailed) return@up

                                                    // store pair and move to next role
                                                    candidateEdgeMap[role] = Pair(down, up);
                                                    return@roles;
                                                }
                                            }
                                        }

                                        if(candidateEdgeMap.isEmpty()) return@candidateTransducerTarget

                                        //we have a set of candidate R associated with the best possible pair of trans edges
                                        //now, we have to check if M <= €rM1 for some R in candidateEdgesMap
                                        //otherwise, reject candidate entry
                                        val resultList: MutableMap<OWLObjectProperty, Int> = mutableMapOf();
                                        candidateEdgeMap.forEach { (role, pairOfEdges) ->
                                            var tcCounter = 0;
                                            ec.tailsets!!.forEach tailsets@{ tailset ->

                                                /**
                                                 * debug line
                                                 */
                                                tcCounter++;
                                                println("Calculating candidate set " + tcCounter + "/ " + candidateMap.size);
                                                if(tcCounter == 10) return@transducerTarget

                                                val restriction =
                                                    restrictionBuilder.createConceptNameRestriction(tailset)
                                                val MClassExp =
                                                    restrictionBuilder.asClassExpression(restriction);
                                                val MExp = expressionBuilder.createELHIExpression(MClassExp);

                                                val entry = SPALoopTableEntry(
                                                    querySource,
                                                    transducerSource,
                                                    queryTarget,
                                                    transducerTarget,
                                                    restriction
                                                )

                                                val costCutoff = table.get(entry) //0, int val or null

                                                candidateResultList.forEach candidates@{ candidateResultPair ->
                                                    //build class expressions
                                                    val M1ClassExp = candidateResultPair.first;
                                                    val candidateCost = candidateResultPair.second;
                                                    val rM1 = expressionBuilder.createExistentialRestriction(role, M1ClassExp)
                                                    val rM1Exp = expressionBuilder.createELHIExpression(rM1);

                                                    //calculate potential result

                                                    /**
                                                     * handle predictable entries separately
                                                     */
//                                                  if(candidate.key.hasEqualSourceAndTarget()) return@candidates

                                                    val remainingCost =
                                                        if (costCutoff == null) null else costCutoff - candidateCost;
                                                    if (costCutoff == 0) return@tailsets; //we cannot improve an entry with cost 0

                                                    /**
                                                     * performance-optimized insert operation.
                                                     * This looks strange, but is a little faster than the obvious way to implement this
                                                     */
                                                    if (costCutoff == null) {
                                                        //check if entailed
                                                          val isEntailed = dlReasoner.checkIsSubsumed(MExp, rM1Exp)
//                                                        val isEntailed = false
                                                        if (isEntailed) {
                                                            //everything in place, this is valid rule application
                                                            //update entry with final value
                                                            val result: Int = pairOfEdges.first.label.cost + candidateCost + pairOfEdges.second.label.cost
                                                            table.set(entry, result);
                                                        }
                                                    }
                                                    else {
                                                        val result: Int = pairOfEdges.first.label.cost + candidateCost + pairOfEdges.second.label.cost
                                                        if(costCutoff > result){
                                                            //check if entailed
                                                          val isEntailed = dlReasoner.checkIsSubsumed(MExp, rM1Exp)
//                                                            val isEntailed = false
                                                            if (isEntailed) {
                                                                //everything in place, this is valid rule application
                                                                //update entry with final value
                                                                table.set(entry, result);
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
                        transEdge.label.outgoing.isConceptAssertion() &&
                        transEdge.label.cost <= maxCost;
            }
        }

        if (candidateTransducerEdges.isEmpty()) {
            return null;
        }

        return Pair(candidateQueryEdges, candidateTransducerEdges)
    }
}