package tin.services.ontology.loopTable.LoopTableBuilder.ELHI

import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHINumericExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTableCompressing
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.*
import tin.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.ELHI.SPANumericLoopTableEntry

class ELHISPALoopTableBuilderCompressing (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELHINumericExecutionContext)
{
    private var table: ELHISPALoopTableCompressing = ELHISPALoopTableCompressing(ec.spaRestrictionBuilder);
    private var updateTable: ELHISPALoopTableCompressing = ELHISPALoopTableCompressing(ec.spaRestrictionBuilder);
    // prepare ontology execution context
//    private val ec = ontologyManager.createELHIExecutionContext(ExecutionContextType.ELHI, false);
//    private val ec = ontologyManager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);

    private val pairsAvailable = mutableSetOf<Pair<Node, Node>>()

    private val s1Calculator = SpaS1CalculatorCompressing(ec, queryGraph, transducerGraph)
    private val s2Calculator = SpaS2CalculatorCompressing(ec, queryGraph, transducerGraph)
    private val s3Calculator = SpaS3CalculatorCompressing(ec, queryGraph, transducerGraph)

    val maxIterationDepth = calculateMaxIterationDepth();

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

    fun calculateInitialS2(): ELHISPALoopTableCompressing {
        //apply S2 to all (q x t)^2 x M
        table = s2Calculator.calculateAll(table);
        return table;
    }


    fun calculateInitialStep(): ELHISPALoopTableCompressing {
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

    fun calculateFullTable(): ELHISPALoopTableCompressing {
        //iterate until max iterations are reached
        initializeTable();


        println("Calculating initial S2 and S3...")

        calculateInitialStep();

        //depth 0

        updateTable = ELHISPALoopTableCompressing(table.map, ec.spaRestrictionBuilder);
        println("Calculating iteration 1 / ${maxIterationDepth}")
        calculateFirstIteration();


        for(i in 2UL..maxIterationDepth) {
            println("Calculating iteration ${i} / ${maxIterationDepth}")
            val hasChanged = calculateNextIteration();
            if(!hasChanged) return table;
        }

        return table;
    }

    private fun calculateS1(isInitialIteration: Boolean) : ELHISPALoopTableCompressing {
        val result = s1Calculator.calculateAll(table, updateTable, isInitialIteration);
        return result;
    }

    private fun calculateS3(): Map<SPANumericLoopTableEntry, Int> {
        val updatedMap = s3Calculator.calculateAllV2(table);
        //contains <spaEntry , Int> pairs that need to be updated in the loop table
        return updatedMap;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }
}