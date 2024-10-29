package tin.services.ontology.loopTable.LoopTableEntryRestriction


interface LoopTableEntryRestriction {
    val value: Any

    fun isEmpty(): Boolean;
}