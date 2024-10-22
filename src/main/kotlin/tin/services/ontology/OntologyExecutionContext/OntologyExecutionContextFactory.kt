package tin.services.ontology.OntologyExecutionContext

import tin.services.ontology.OntologyManager

class OntologyExecutionContextFactory {

    fun create(executionContextType: ExecutionContextType, manager: OntologyManager, prewarmCaches: Boolean = false): ExecutionContext {
        when(executionContextType) {
            ExecutionContextType.LOOPTABLE -> {
                val ec = OntologyExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.ELHI -> {
                val ec = ELHIExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }
        }
    }
}