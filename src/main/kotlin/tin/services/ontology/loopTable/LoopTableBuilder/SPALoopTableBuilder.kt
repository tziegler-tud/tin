package tin.services.ontology.loopTable.LoopTableBuilder

import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS1Calculator

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

    private val restrictionBuilder = ec.restrictionBuilder;

    private val finished: Boolean = false;

    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>();
    private val tailsets = ec.tailsets!!;

    private val s1Calculator = SpaS1Calculator(ec, queryGraph, transducerGraph)

    init {


    }

    private fun calculateMaxIterationDepth() : Int {
        //calculate iteration depth based on ontology signature
        return 0;
    }

    private fun initializeTable(){
        //build all pairs for (s,t) € queryNodes x transducerNodes
        queryGraph.nodes.forEach { node ->
            transducerGraph.nodes.forEach { transducerNode ->
                pairsAvailable.add(Pair(node, transducerNode))
            }
        }



    }

    fun calculateNextIteration(){

        //for each (p,q,M), perform S1 step
        pairsAvailable.forEach{ source ->
            pairsAvailable.forEach { target ->
                tailsets.forEach { tailset ->
                    //foreach (p,q,M) do:

                    val restriction = restrictionBuilder.createConceptNameRestriction(tailset)
                    val entry = SPALoopTableEntry(source, target, restriction)
                    calculateS1(entry);
                }
            }
        };

        calculateS3();

    }

    fun calculateFullTable(): SPALoopTable {
        //iterate until max iterations are reached
        initializeTable();
        calculateNextIteration();

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
//
//        val costCutoff = table.get(spaLoopTableEntry) //0, int val or null
//        val source = spaLoopTableEntry.source;
//        val target = spaLoopTableEntry.target;
//        val M = spaLoopTableEntry.restriction;
//        val s = source.first;
//        val t = source.second;
//        val se = target.first;
//        val te = target.second;
//
//        val MCLassExp = restrictionBuilder.asClassExpression(M)
//        val MExp = expressionBuilder.createELHIExpression(MCLassExp)
//
        s1Calculator.calculate(spaLoopTableEntry, table)



        /**
         * NEW Calculation:
         * 0. Get Candidates (p,q,M1)
         * 1. For each Role R:
         *    Build superclass R' s.t. R <= R'
         *    Build superclass R'' s.t. R(-) <= R''
         *
         *  1.1. For each candidate:
         *      calculate Subclass M <= E R. M1
         *      These are the M's that can could be updated using the respective role
         *      Store entry for M with cost of candidate set
         *
         *  1.2. For each symetric pair (s,t),(s,t):
         *      Calculate Subclass M <= E R . TOP
         *      Store entry for M with cost 0
         *
         *  1.3 Use stored M's and foreach:
         *
         *      for each (s,t), (s',t'): calculate minimum weights w1, w2
         *
         */





    }

    private fun calculateS2(){


    }

    private fun calculateS3(){

    }

}