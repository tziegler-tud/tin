package tin.services.ontology.loopTable.LoopTableBuilder.ELHI

import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.loopTable.ELHISPALoopTable

interface SpaS1RuleCalculator {

    fun calculateAll(
        table: ELHISPALoopTable,
        updateTable: ELHISPALoopTable,
        isInitialIteration: Boolean
    ): ELHISPALoopTable;

}