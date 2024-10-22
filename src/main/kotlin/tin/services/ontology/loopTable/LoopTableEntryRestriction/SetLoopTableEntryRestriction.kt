package tin.services.ontology.loopTable.LoopTableEntryRestriction

interface SetLoopTableEntryRestriction<DataType> : LoopTableEntryRestriction<DataType> {
    override val value: Set<DataType>
}