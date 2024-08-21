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
    init {


    }

    private fun calculateMaxIterationDepth() : Int {
        //calculate iteration depth based on ontology signature
        return 0;
    }

    fun initializeTable(){

        //initialize [p,p,M] = 0
        //create pairs (s,t),(s,t)
        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->


            }
        }


    }

    fun calculateNextIteration(){

    }

    fun calculateFullTable(): SPALoopTable {
        //iterate until max iterations are reached



        return table;
    }

}