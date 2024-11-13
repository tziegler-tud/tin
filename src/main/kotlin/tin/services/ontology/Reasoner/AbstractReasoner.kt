package tin.services.ontology.Reasoner

abstract class AbstractReasoner: DLReasoner {

    open override fun clearCache() {
        return
    }

    open override fun getStats() : Map<String, Int> {
        return mapOf<String, Int>();
    }
}