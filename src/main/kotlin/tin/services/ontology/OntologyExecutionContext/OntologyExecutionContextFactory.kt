package tin.services.ontology.OntologyExecutionContext

import tin.services.ontology.OntologyManager

class OntologyExecutionContextFactory {

    fun create(executionContextType: ExecutionContextType, manager: OntologyManager, prewarmCaches: Boolean = false): OntologyExecutionContext {
        when(executionContextType) {
            ExecutionContextType.LOOPTABLE -> {
                val ec = OntologyExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.LOOPTABLE_NUMERIC -> {
                val ec = OntologyExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }
        }
    }
}