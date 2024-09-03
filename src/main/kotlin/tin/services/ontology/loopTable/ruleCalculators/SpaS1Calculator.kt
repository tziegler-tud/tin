package tin.services.ontology.loopTable.ruleCalculators

import org.semanticweb.owlapi.model.OWLObjectProperty
import tin.services.ontology.DLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilder
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SpaS1Calculator(private val restrictionBuilder: RestrictionBuilder, private val expressionBuilder: DLExpressionBuilder, private val dlReasoner: DLReasoner, private val ec: OntologyExecutionContext) {

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
    private fun calculate(spaLoopTableEntry: SPALoopTableEntry, table: SPALoopTable): Int {
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

        //iterate through candidates and perform steps 2.1 - 2.6
        candidateMap.forEach { (candidateEntry, candidateCost) ->
            val s1 = candidateEntry.source.first;
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

            candidateRolesFromTable.forEach { role ->

                //for each r, calculate superclass r'
                val super_r = dlReasoner.calculateSuperProperties(role);
                //we need to add the role itself, as it is not included in the superroles calculated by the reasoner

                val inv_super_r = dlReasoner.calculateSuperProperties(role.getInverseProperty());

                //both sets should have the same cardinality
                //more specifically, the second set contains the inverse role of each one in the first
                //this also means we would not have to calculate both.



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