package tin.services.ontology.loopTable.old.okt1824.LoopTableBuilder

import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyExecutionContext.OntologyExecutionContext
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.SPALoopTable
import tin.services.ontology.loopTable.loopTableEntry.SPALoopTableEntry
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS1Calculator
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS2Calculator
import tin.services.ontology.loopTable.LoopTableBuilder.ruleCalculators.SpaS3Calculator

class SPALoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager)
{
    private var table: SPALoopTable = SPALoopTable();
    private var updateTable: SPALoopTable = SPALoopTable();
    // prepare ontology execution context
    private val ec = ontologyManager.createExecutionContext(ExecutionContextType.LOOPTABLE, false);
    private val dlReasoner = ec.dlReasoner;
    private val expressionBuilder = ec.expressionBuilder;
    private val queryParser = ec.parser;
    private val shortFormProvider = ec.shortFormProvider;

    private val restrictionBuilder = ec.restrictionBuilder;

    private val finished: Boolean = false;

    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>();
    private val tailsets = ec.tailsets!!;

    private val s1Calculator = SpaS1Calculator(ec, queryGraph, transducerGraph)
    private val s2Calculator = SpaS2Calculator(ec, queryGraph, transducerGraph)
    private val s3Calculator = SpaS3Calculator(ec, queryGraph, transducerGraph)

    public val maxIterationDepth = calculateMaxIterationDepth();


    init {


    }

    fun prewarmSubsumptionCahce(){
        ec.prewarmSubsumptionCache();
    }

    private fun calculateMaxIterationDepth() : Int {
        //calculate iteration depth based on ontology signature
        val queryTransSize = queryGraph.nodes.size * transducerGraph.nodes.size;
        return queryTransSize * queryTransSize * ec.tailsets!!.size;
    }

    private fun initializeTable(){
        //build all pairs for (s,t) € queryNodes x transducerNodes
        queryGraph.nodes.forEach { node ->
            transducerGraph.nodes.forEach { transducerNode ->
                pairsAvailable.add(Pair(node, transducerNode))
//                ec.tailsets!!.forEach { tailset ->
//                    val restriction = restrictionBuilder.createConceptNameRestriction(tailset);
//                    table.set(SPALoopTableEntry(node, transducerNode, node, transducerNode, restriction), 0);
//                }
            }
        }
    }

    fun calculateInitialS2V2(): SPALoopTable {
        //apply S2 to all (q x t)^2 x M
        table = s2Calculator.calculateAll(table);
        return table;
    }

    fun calculateInitialS2(): SPALoopTable {
        //apply S2 to all (q x t)^2 x M
        pairsAvailable.forEach{ source ->
            pairsAvailable.forEach target@{ target ->
                if(source.first == target.first && source.second == target.second) return@target;
                tailsets.forEach tailset@{ tailset ->
                    //foreach (p,q,M) do:
                    val restriction = restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset)
                    val entry = SPALoopTableEntry(source, target, restriction)
                    // dont add table entries (q,t)(q,t),_
                    val updatedValue = calculateS2(entry)
                    if(updatedValue !== null) {
                        table.set(entry, updatedValue );
                    }
                }
            }
        };
        return table;
    }

    fun calculateInitialStep(): SPALoopTable {
        calculateInitialS2V2();
        //apply S3
        calculateS3();
        return table;
    }

    fun calculateNextIteration(){

        var counter : Int;

        //for each (p,q,M), perform S1 step
        pairsAvailable.forEach{ source ->
            pairsAvailable.forEach target@{ target ->
                counter = 0;
                if(source.first == target.first && source.second == target.second) return@target;
                tailsets.forEach { tailset ->
                    counter++;
                    println("Calculating tailset " + counter + " / " + tailsets.size)
//                    if(counter > 2) return@target;
                    //foreach (p,q,M) do:
                    val restriction = restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset)
                    val entry = SPALoopTableEntry(source, target, restriction)
                    val updatedValue = calculateS1(entry)
                    if(updatedValue !== null) {
                        table.set(entry, updatedValue );
                    }
                }
            }
        };
        calculateS3V2();
    }

    fun calculateFirstIterationV2(){

        var counter : Int;

        updateTable = calculateS1V2(true);
        updateTable.map.forEach { (entry, value) ->
            table.set(entry, value);
        }

        val updateMap = calculateS3V2();
        updateMap.forEach { (entry, value) ->
            val current = table.get(entry)
            if(current == null || value < current) {
                updateTable.set(entry, value)
                table.set(entry, value);
            };
        }
    }

    fun calculateNextIterationV2() : Boolean {

        var counter : Int;

        updateTable = calculateS1V2(false);
        updateTable.map.forEach { (entry, value) ->
            table.set(entry, value);
        }

        //if nothing changed, no need to run S3 rule again
        //TODO: do we even have to continue in this case? Is one iteration without changes enough to abort?
        //TODO: This is important!
        if(updateTable.map.isEmpty()) return false

        val updateMap = calculateS3V2();
        updateMap.forEach { (entry, value) ->
            val current = table.get(entry)
            if(current == null || value < current) {
                updateTable.set(entry, value)
                table.set(entry, value);
            };
        }
        return true;
    }

    fun calculateFullTable(): SPALoopTable {
        //iterate until max iterations are reached
        initializeTable();

        calculateInitialStep();

        //depth 0

        updateTable = SPALoopTable(table.map);
        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIterationV2();


        for(i in 2..maxIterationDepth) {
            println("Calculating iteration ${i} / ${maxIterationDepth}")
            val hasChanged = calculateNextIterationV2();
            if(!hasChanged) return table;
        }

        return table;
    }

    fun calculateFullTableV2(): SPALoopTable {
        //iterate until max iterations are reached
        initializeTable();

        calculateInitialStep();

        //depth 0

        updateTable = SPALoopTable(table.map);
        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIterationV2();


        for(i in 2..maxIterationDepth) {
            println("Calculating iteration ${i} / ${maxIterationDepth}")
            val hasChanged = calculateNextIterationV2();
            if(!hasChanged) return table;
        }

        return table;
    }

    fun calculateWithDepthLimit(limit: Int): SPALoopTable {
        //iterate until max iterations are reached
        println("Calculating loop table for paths up to depth ${limit}...")
        initializeTable();

        calculateInitialStep();

        updateTable = SPALoopTable(table.map);
        //depth 0

        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIterationV2();

        for(i in 2..limit) {
            println("Calculating iteration ${i} / ${limit}")
            calculateNextIterationV2();
        }
        return table;
    }

    private fun calculateS1(spaLoopTableEntry: SPALoopTableEntry): Int? {
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
        val result = s1Calculator.calculate(spaLoopTableEntry, table);
        //result is either an int, meaning this is the updated value, or null if no value could be obtained
        return result;
    }

    private fun calculateS1V2(isInitialIteration: Boolean) : SPALoopTable {
        val result = s1Calculator.calculateAllV2(table, updateTable, isInitialIteration);
        return result;
    }

    private fun calculateS2(spaLoopTableEntry: SPALoopTableEntry) : Int? {

        val result = s2Calculator.calculate(spaLoopTableEntry, table)
        return result;
    }

    private fun calculateS3() {
        for (tailset in tailsets) {
            val restriction = restrictionBuilder.createConceptNameRestrictionFromStringSet(tailset);
            val updatedMap = s3Calculator.calculateAll(restriction, table);
            //contains <spaEntry , Int> pairs that need to be updated in the loop table
            updatedMap.forEach { (entry, value) ->
                table.set(entry, value);
            }
        }
    }

    private fun calculateS3V2(): Map<SPALoopTableEntry, Int> {
        val updatedMap = s3Calculator.calculateAllV2(table);
        //contains <spaEntry , Int> pairs that need to be updated in the loop table
        return updatedMap;
    }

    public fun getExecutionContext(): OntologyExecutionContext {
        return ec;
    }
}