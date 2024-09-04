package tin.services.ontology.loopTable.LoopTableEntryRestriction

class ConceptNameRestriction(override val value: HashSet<String>) : LoopTableEntryRestriction<Any> {
    fun asSet(): HashSet<String> {
        return value;
    }


}