package tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS1Calculator
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS2Calculator
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS3Calculator
import tinDL.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry

class ELHISPALoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELHIExecutionContext) : ELHILoopTableBuilder
{
    private var table: ELHISPALoopTable = ELHISPALoopTable();
    private var updateTable: ELHISPALoopTable = ELHISPALoopTable();

    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>()

    private val s1Calculator = SpaS1Calculator(ec, queryGraph, transducerGraph)
    private val s2Calculator = SpaS2Calculator(ec, queryGraph, transducerGraph)
    private val s3Calculator = SpaS3Calculator(ec, queryGraph, transducerGraph)

    //stat tracking
    var statsTotalIterations: Int = 0
    var statsTotalSize: Int = 0;
    val statsMaxPossibleSize: ULong = (queryGraph.nodes.size * transducerGraph.nodes.size).toULong() * ec.tailsetSize;


    public val maxIterationDepth = calculateMaxIterationDepth();

    fun prewarmSubsumptionCache(){
        ec.prewarmSubsumptionCache();
    }

    private fun calculateMaxIterationDepth() : ULong {
        //calculate iteration depth based on ontology signature
        val queryTransSize = queryGraph.nodes.size * transducerGraph.nodes.size;
        return queryTransSize.toULong() * queryTransSize.toULong() * ec.tailsetSize;
    }

    private fun initializeTable(){
        //build all pairs for (s,t) € queryNodes x transducerNodes
        queryGraph.nodes.forEach { node ->
            transducerGraph.nodes.forEach { transducerNode ->
                pairsAvailable.add(Pair(node, transducerNode))
            }
        }
    }

    fun calculateInitialS2(): ELHISPALoopTable {
        //apply S2 to all (q x t)^2 x M
        table = s2Calculator.calculateAll(table);
        return table;
    }


    fun calculateInitialStep(): ELHISPALoopTable {
        println("Calculating initial S2 rule applications...")

        calculateInitialS2();
        //apply S3
        println("Calculating initial S3 rule applications...")
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

    fun calculateFullTable(): ELHISPALoopTable {
        //iterate until max iterations are reached
        initializeTable();


        println("Calculating initial S2 and S3...")

        calculateInitialStep();

        //depth 0

        updateTable = ELHISPALoopTable(table.map);
        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIteration();


        for(i in 2UL..maxIterationDepth) {
            statsTotalIterations = i.toInt()
            println("Calculating iteration ${i} / ${maxIterationDepth}")
            val hasChanged = calculateNextIteration();
            if(!hasChanged) return table;
        }
        return table;
    }

    fun calculateWithDepthLimit(limit: Int): ELHISPALoopTable {
        //iterate until max iterations are reached
        println("Calculating loop table for paths up to depth ${limit}...")
        initializeTable();

        calculateInitialStep();

        updateTable = ELHISPALoopTable(table.map);
        //depth 0

        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIteration();

        for(i in 2..limit) {
            println("Calculating iteration ${i} / ${limit}")
            calculateNextIteration();
        }
        return table;
    }

    private fun calculateS1(isInitialIteration: Boolean) : ELHISPALoopTable {
        val result = s1Calculator.calculateAll(table, updateTable, isInitialIteration);
        return result;
    }

    private fun calculateS3(): Map<ELHISPALoopTableEntry, Int> {
        val updatedMap = s3Calculator.calculateAllV2(table);
        //contains <spaEntry , Int> pairs that need to be updated in the loop table
        return updatedMap;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }

    fun getSize(): Int {
        return table.map.size;
    }
}