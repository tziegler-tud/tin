package tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators

import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.DLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

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
    fun calculate(spaLoopTableEntry: SPALoopTableEntry, table: SPALoopTable): Int {
        val source = spaLoopTableEntry.source;
        val target = spaLoopTableEntry.target;
        val M = spaLoopTableEntry.restriction;
        val s = source.first;
        val t = source.second;
        val se = target.first;
        val te = target.second;

        val costCutoff = table.get(spaLoopTableEntry) //0, int val or null

        if (costCutoff == 0) return 0; //we cannot improve an entry with cost 0

        val MCLassExp = restrictionBuilder.asClassExpression(M);
        val MExp = expressionBuilder.createELHIExpression(MCLassExp);

        //list all entries < costCutoff
        //these are the non-trivial candidate entries spa[(s1,t1),(s2,t2),M1] that need to be checked
        val candidateMap = table.getWithCostLimit(costCutoff)

        var candidateRolesFromTable: Set<OWLObjectProperty>
        var candidateRolesSym: Set<OWLObjectProperty>;

        var minimumCostCandidate : SPALoopTableEntry? = null;
        var minimumCost: Int = -1;

        //iterate through candidates and perform steps 2.1 - 2.6
        candidateMap.forEach { (candidateEntry, candidateCost) ->
            val s1: Node = candidateEntry.source.first;
            val s2 = candidateEntry.target.first;
            val t1 = candidateEntry.source.second;
            val t2 = candidateEntry.target.second;
            val M1 = candidateEntry.restriction;

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
                //we need to add the role itself, as it is not included in the superroles calculated by the reasoner
                val superRShortForms = superR.map {
                    shortFormProvider.getShortForm(it.representativeElement.asOWLObjectProperty());
                }.toMutableList();
                superRShortForms.add(shortFormProvider.getShortForm(role));

//                val invSuperR = dlReasoner.calculateSuperProperties(role.getInverseProperty());
//                val invSuperRShortForms = invSuperR.map {
//                    shortFormProvider.getShortForm(it.representativeElement.asOWLObjectProperty());
//                }.toMutableList();
//                invSuperRShortForms.add(shortFormProvider.getShortForm(role.inverseProperty.asOWLObjectProperty()));

                //both sets should have the same cardinality
                //more specifically, the second set contains the inverse role of each one in the first
                //this also means we would not have to calculate both.


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
                var candidateTransducerEdges = transducerGraph.getEdgesWithSourceAndTarget(t, t1);
                if(candidateTransducerEdges.isEmpty()) {
                    return@lit;
                }
                val sortedTransducerEdges = candidateTransducerEdges.sortedBy { it.label.cost }

                //find the minimal cost of an edge (t,u,R',w,t1) with:
                //  u in candidateQueryTransitions
                //  R' in superR

                val minCostEdgeDown = null;
                sortedTransducerEdges.forEach edgeCheck@ { transducerEdge ->
                    val inLabel = transducerEdge.label.incoming;
                    val outLabel = transducerEdge.label.outgoing;

                    //outgoing label must be a role name, not a concept assertion;
                    if(outLabel.isConceptAssertion()) return@edgeCheck;
                    // try to match edge label to positive role name
                    var edgeProperty: OWLObjectPropertyExpression = queryParser.getOWLObjectProperty(outLabel) ?: return@edgeCheck;
                    //if no property could be obtained, there is no way to continue - this also means our transducer uses property names which are not part of our ontology.



                    val isValidQueryTransition = candidateQueryTransitions.contains(QueryEdgeLabel(inLabel))
                            && superR.contains(edgeProperty);
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

                val sortedEdgesUp = candidateTransducerEdgesUp.sortedByDescending { it.label.cost }
                val minCostEdgeUp = sortedEdgesUp.findLast {
                    candidateQueryTransitions.contains(it.label.incoming) &&
                            superRShortForms.contains(it.label.outgoing)
                }

                if(minCostEdgeUp == null) {
                    return@lit;
                }

                val localCost = minCostEdgeDown.cost + candidateCost + minCostEdgeUp.cost
                if (localCost < minimumCost || minimumCost == -1){
                    minimumCost = localCost;
                    minimumCostCandidate = candidateEntry;
                }
            }
        }

        return 0;

//        queryGraph.nodes.forEach {
//            transducerGraph.nodes.forEach { transducerNode ->
//                tailsets.forEach { tailset ->
//                    candidateRolesSym
//                }
//            }
//        }
    }
}