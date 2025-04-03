package tinDL.services.ontology.loopTable.LoopTable

interface MutableLoopTable<entryType> : LoopTable<entryType> {

    fun set(entry: entryType, value: Int)
    /**
     * updates the entry with the given value IF the current value is null or greater than the given value.
     * Returns true if the value was updated, false otherwise
     */
    fun setIfLower(entry: entryType, value: Int) : Boolean

}