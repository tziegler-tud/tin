package tin.services.ontology.loopTable.old.okt1824.LoopTableEntryRestriction

interface LoopTableEntryRestriction<DataType> {
    val value: Set<DataType>

    fun isEmpty(): Boolean;

    fun asSet(): Set<DataType>;

    fun asList(): List<DataType>;

    fun containsElement(element: DataType): Boolean;
}