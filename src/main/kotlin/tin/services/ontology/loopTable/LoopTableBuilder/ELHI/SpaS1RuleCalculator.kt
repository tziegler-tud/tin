package tin.services.ontology.loopTable.LoopTableBuilder.ELHI

import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPALoopTable

interface SpaS1RuleCalculator {

    fun calculateAll(
        table: ELHISPALoopTable,
        updateTable: ELHISPALoopTable,
        isInitialIteration: Boolean
    ): ELHISPALoopTable;

}