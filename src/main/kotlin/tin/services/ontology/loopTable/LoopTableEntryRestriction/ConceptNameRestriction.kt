package tin.services.ontology.loopTable.LoopTableEntryRestriction

import tin.model.graph.Node

class ConceptNameRestriction(override val value: HashSet<String>) : LoopTableEntryRestriction<Any> {
    fun asSet(): HashSet<String> {
        return value;
    }


}