package tin.services.ontology.loopTable.LoopTableBuilder

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

    fun calculateInitialS2(): SPALoopTable {
        //apply S2 to all (q x t)^2 x M
        table = s2Calculator.calculateAll(table);
        return table;
    }


    fun calculateInitialStep(): SPALoopTable {
        calculateInitialS2();
        //apply S3
        calculateS3();
        return table;
    }

    fun calculateFirstIteration(){

        var counter : Int;

        updateTable = calculateS1(true);
        updateTable.map.forEach { (entry, value) ->
            table.set(entry, value);
        }

        val updateMap = calculateS3();
        updateMap.forEach { (entry, value) ->
            val current = table.get(entry)
            if(current == null || value < current) {
                updateTable.set(entry, value)
                table.set(entry, value);
            };
        }
    }

    fun calculateNextIteration() : Boolean {

        var counter : Int;

        updateTable = calculateS1(false);
        updateTable.map.forEach { (entry, value) ->
            table.set(entry, value);
        }

        //if nothing changed, no need to run S3 rule again
        //TODO: do we even have to continue in this case? Is one iteration without changes enough to abort?
        //TODO: This is important!
        if(updateTable.map.isEmpty()) return false

        val updateMap = calculateS3();
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
        calculateFirstIteration();


        for(i in 2..maxIterationDepth) {
            println("Calculating iteration ${i} / ${maxIterationDepth}")
            val hasChanged = calculateNextIteration();
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
        calculateFirstIteration();

        for(i in 2..limit) {
            println("Calculating iteration ${i} / ${limit}")
            calculateNextIteration();
        }
        return table;
    }

    private fun calculateS1(isInitialIteration: Boolean) : SPALoopTable {
        val result = s1Calculator.calculateAllV2(table, updateTable, isInitialIteration);
        return result;
    }

    private fun calculateS2(spaLoopTableEntry: SPALoopTableEntry) : Int? {

        val result = s2Calculator.calculate(spaLoopTableEntry, table)
        return result;
    }

    private fun calculateS3(): Map<SPALoopTableEntry, Int> {
        val updatedMap = s3Calculator.calculateAllV2(table);
        //contains <spaEntry , Int> pairs that need to be updated in the loop table
        return updatedMap;
    }

    public fun getExecutionContext(): OntologyExecutionContext {
        return ec;
    }
}