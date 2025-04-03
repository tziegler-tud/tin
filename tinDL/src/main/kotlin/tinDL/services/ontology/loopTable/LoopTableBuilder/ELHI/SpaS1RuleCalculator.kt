package tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI

import tinDL.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable

interface SpaS1RuleCalculator {

    fun calculateAll(
        table: ELHISPALoopTable,
        updateTable: ELHISPALoopTable,
        isInitialIteration: Boolean
    ): ELHISPALoopTable;

}