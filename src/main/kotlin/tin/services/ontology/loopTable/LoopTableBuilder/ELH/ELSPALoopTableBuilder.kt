package tin.services.ontology.loopTable.LoopTableBuilder.ELH

import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable.ELH.ELSPALoopTable
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators.SpaS1Calculator
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators.SpaS2Calculator
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ruleCalculators.SpaS3Calculator
import tin.services.ontology.loopTable.loopTableEntry.ELH.ELSPALoopTableEntry

class ELSPALoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELExecutionContext
) : ELLoopTableBuilder
{
    private var table: ELSPALoopTable = ELSPALoopTable();
    private var updateTable: ELSPALoopTable = ELSPALoopTable();
    // prepare ontology execution context
//    private val ec = ontologyManager.createELHIExecutionContext(ExecutionContextType.ELHI, false);
//    private val ec = ontologyManager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);

    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>()

    private val s1Calculator = SpaS1Calculator(ec, queryGraph, transducerGraph)
    private val s2Calculator = SpaS2Calculator(ec, queryGraph, transducerGraph)
    private val s3Calculator = SpaS3Calculator(ec, queryGraph, transducerGraph)

    //stat tracking
    var statsTotalIterations: Int = 0
    var statsTotalSize: Int = 0;
    val statsMaxPossibleSize: ULong = (queryGraph.nodes.size * transducerGraph.nodes.size * ec.tailsetSize).toULong();

    public val maxIterationDepth = calculateMaxIterationDepth();

    fun prewarmSubsumptionCache(){
        ec.prewarmSubsumptionCache();
    }

    private fun calculateMaxIterationDepth() : Int {
        //calculate iteration depth based on ontology signature
        val queryTransSize = queryGraph.nodes.size * transducerGraph.nodes.size;
        return queryTransSize * queryTransSize * ec.tailsetSize;
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

    fun calculateInitialS2(): ELSPALoopTable {
        //apply S2 to all (q x t)^2 x M
        table = s2Calculator.calculateAll(table);
        return table;
    }


    fun calculateInitialStep(): ELSPALoopTable {
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

    fun calculateFullTable(): ELSPALoopTable {
        //iterate until max iterations are reached
        statsTotalIterations = 0;
        statsTotalSize = 0;
        initializeTable();

        calculateInitialStep();

        //depth 0

        updateTable = ELSPALoopTable(table.map);
        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIteration();


        for(i in 2..maxIterationDepth) {
            statsTotalIterations = i
            println("Calculating iteration ${i} / ${maxIterationDepth}")
            val hasChanged = calculateNextIteration();
            if(!hasChanged) return table;
        }
        statsTotalSize = table.map.size;
        return table;
    }

    private fun calculateS1(isInitialIteration: Boolean) : ELSPALoopTable {
        val result = s1Calculator.calculateAll(table, updateTable, isInitialIteration);
        return result;
    }

    private fun calculateS3(): Map<ELSPALoopTableEntry, Int> {
        val updatedMap = s3Calculator.calculateAllV2(table);
        //contains <spaEntry , Int> pairs that need to be updated in the loop table
        return updatedMap;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }
}