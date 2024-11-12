package tin.services.ontology.OntologyExecutionContext

import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.OntologyExecutionContext.EL.ELSetExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHINumericExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHISetExecutionContext
import tin.services.ontology.OntologyManager

class OntologyExecutionContextFactory {

    fun create(executionContextType: ExecutionContextType, manager: OntologyManager, prewarmCaches: Boolean = false): ExecutionContext {
        when(executionContextType) {
            ExecutionContextType.LOOPTABLE -> {
                val ec = ELHISetExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.ELHI -> {
                val ec = ELHISetExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.ELHI_NUMERIC -> {
                val ec = ELHINumericExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.ELH -> {
                val ec = ELSetExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }
        }
    }

    fun createELHIContext(executionContextType: ExecutionContextType, manager: OntologyManager, prewarmCaches: Boolean = false): ELHIExecutionContext {
        when(executionContextType) {
            ExecutionContextType.LOOPTABLE -> {
                throw Error("Unsupported execution context type");
            }

            ExecutionContextType.ELHI -> {
                val ec = ELHISetExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.ELHI_NUMERIC -> {
                val ec = ELHINumericExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }

            ExecutionContextType.ELH -> {
                throw Error("Unsupported execution context type");
            }
        }
    }

    fun createELContext(executionContextType: ExecutionContextType, manager: OntologyManager, prewarmCaches: Boolean = false): ELExecutionContext {
        when(executionContextType) {
            ExecutionContextType.ELH -> {
                val ec = ELSetExecutionContext(manager);
                ec.prepareForLoopTableConstruction(prewarmCaches)
                return ec;
            }
            else -> {
                throw Error("Unsupported execution context type");
            }
        }
    }
}