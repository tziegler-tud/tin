package tin.services.ontology.loopTable.LoopTableEntryRestriction

interface NumericLoopTableEntryRestriction<DataType> : LoopTableEntryRestriction<DataType> {
    override val value: ULong
}