package tin.services.ontology.loopTable.LoopTableBuilder.ELHI

import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpCalculator

class ELHISPLoopTableBuilder (
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val ontologyManager: OntologyManager,
    private val ec: ELHIExecutionContext)
{
    private var table: ELHISPLoopTable = ELHISPLoopTable();
    private val calculator = SpCalculator(ec, queryGraph, transducerGraph)


    fun calculateFullTable(spaTable: ELHISPALoopTable): ELHISPLoopTable {
        //iterate until max iterations are reached
        table = calculator.calculateAll(spaTable)
        return table;
    }

    public fun getExecutionContext(): ExecutionContext {
        return ec;
    }
}