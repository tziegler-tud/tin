package tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpCalculator

class ELHISPLoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELHIExecutionContext) : ELHILoopTableBuilder
{
    private var table: ELHISPLoopTable = ELHISPLoopTable();
    private val calculator = SpCalculator(ec, queryGraph, transducerGraph)

    //stat tracking
    var statsTotalSize: Int = 0;
    val statsMaxPossibleSize: Int = (queryGraph.nodes.size * transducerGraph.nodes.size * ec.individuals.size);


    fun calculateFullTable(spaTable: ELHISPALoopTable): ELHISPLoopTable {
        //iterate until max iterations are reached
        table = calculator.calculateAll(spaTable)
        statsTotalSize = table.getSize();
        return table;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }

    fun getSize(): Int {
        return table.map.size;
    }
}