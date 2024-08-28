package tin.services.ontology.loopTable.LoopTableBuilder

import tin.model.query.QueryGraph
import tin.model.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.LoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry

class SPALoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager)

{
    private val maxIterationDepth = calculateMaxIterationDepth();
    private val table: SPALoopTable = SPALoopTable();
    // prepare ontology execution context
    private val ec = ontologyManager.createExecutionContext(ExecutionContextType.LOOPTABLE);
    private val dlReasoner = ec.dlReasoner;
    private val expressionBuilder = ec.expressionBuilder;
    private val queryParser = ec.parser;
    private val shortFormProvider = ec.shortFormProvider;

    private val finished: Boolean = false;
    init {


    }

    private fun calculateMaxIterationDepth() : Int {
        //calculate iteration depth based on ontology signature
        return 0;
    }

    fun initializeTable(){

        //initialize [p,p,M] = 0
        //create pairs (s,t),(s,t)

    }

    fun calculateNextIteration(){

    }

    fun calculateFullTable(): SPALoopTable {
        //iterate until max iterations are reached



        return table;
    }

    private fun calculateS1(spaLoopTableEntry: SPALoopTableEntry){
        /**
         *  Given: (s,t),(s',t'),M
         *  spa[(s,t),(s',t'),M] <-- we call this value costCutoff and use it to discard a run if this is superseded at any time
         *  Need to guess: M_1, r, r', r'',
         *  (s_1,t_1), (s_2,t_2) --> only makes sense if spa[(s_1,t_1),(s_2,t_2),M_1 is set and <
         *
         *
         *    1. Determine candidate sets (s_1,t_1),(s_2,t_2),M_1 --> these are basically all entry in spa that are < +inf
         *       We have to check identical pairs as well, but here M_1 is arbitrary
         *
         *     1.1 Add all existing entries in spa (s_1,t_1),(s_2,t_2),M_1 as candidates.
         *     1.2. Add all symmetric pairs with arbitrary M_1 (s_1,t_1),(s_1,t_1). These can never be in the first set
         *
         *    2. For each candidate with M set, do the following:
         *     At this point, we have: (s_1,t_1),(s_2,t_2),M_1 available
         *     2.1 Calculate set of candidate roles r s.t. M <= E r . M_1
         *     2.2 Calculate superclass R' s.t. R <= R'
         *     2.3 Calculate superclass R'' s.t. inv(R) <= R''
         *     2.4 Find u, u' s.t. (s,u,s_1), (s_2,u',s') € queryTransitions
         *     2.5 For each u, do:
         *      2.5.1 Find the minimum cost edge with (t,u,R',w_1,t_1) € transducerEdges for some w_1 < costCutoff
         *      2.5.2 if no edge is found, discard u and try again with next
         *      2.5.3 If found, mark u as okay and associate w_1
         *     2.6 For each u', do:
         *     2.6.1 Find some (t_2,u',R'',w_2,t') € transducerEdges for some w_2 < costCutoff
         *     2.6.2 if no edge is found, discard u' and try again with next
         *     2.6.3 If found, mark u' as okay and associate w_2
         *
         */

        val costCutoff = table.get(spaLoopTableEntry) //0, int val or null
        val source = spaLoopTableEntry.source;
        val target = spaLoopTableEntry.target;
        val s = source.first;
        val t = source.second;
        val se = target.first;
        val te = target.second;

        //list all entries < costCutoff
        val candidateMap = table.getWithCostLimit(costCutoff)

        //iterate through candidates and perform steps 2.1 - 2.6
        candidateMap.forEach { (key, value) ->
            val s1 = key.source.first;
            val s2 = key.target.first;
            val t1 = key.source.second;
            val t2 = key.target.second;
            val M1 = key.restriction;
            val roleNames = ec.getRoleNames();
            val roles = ec.getRoles();

            //calculate candidate role names r s.t. M <= E r. M1
            //for each role, check if entailed
            roles.forEach { role ->
                //build class expressions
                expressionBuilder.createELHIExpression()
                val isEntailed = dlReasoner.checkIsSubsumed()
            }

        }





    }

    private fun calculateS2(){

    }

    private fun calculateS3(){

    }

}