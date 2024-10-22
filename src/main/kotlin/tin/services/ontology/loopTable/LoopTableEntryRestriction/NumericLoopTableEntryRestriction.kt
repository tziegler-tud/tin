package tin.services.ontology.loopTable.LoopTableEntryRestriction

interface NumericLoopTableEntryRestriction<DataType> {
    val value: Long

    fun isEmpty(): Boolean;

    fun asSet(): Set<DataType>;

    fun asList(): List<DataType>;

    fun containsElement(element: DataType): Boolean;
}