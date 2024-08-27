package tin.services.ontology.OntologyExecutionContext

import tin.services.ontology.OntologyManager

class OntologyExecutionContextFactory {

    fun create(executionContextType: ExecutionContextType, manager: OntologyManager): OntologyExecutionContext {
        when(executionContextType) {
            ExecutionContextType.LOOPTABLE -> {
                val ec = OntologyExecutionContext(manager);
                ec.prepareForLoopTableConstruction()
                return ec;
            }
        }
    }
}