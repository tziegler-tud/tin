package tin.services.ontology.loopTable.LoopTableBuilder.ELHI

import tin.model.v2.query.QueryGraph
import tin.model.v2.graph.Node
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.ELHISPALoopTable
import tin.services.ontology.loopTable.ELHISPLoopTable
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpCalculator
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS1Calculator
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS2Calculator
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ruleCalculators.SpaS3Calculator
import tin.services.ontology.loopTable.loopTableEntry.ELHISPALoopTableEntry

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