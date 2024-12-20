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
}