package tinDL.services.ontology.OntologyExecutionContext.EL

import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.*

interface ELExecutionContext : ExecutionContext {
    override val spaRestrictionBuilder: SingleClassRestrictionBuilderInterface
    override val spRestrictionBuilder: SingleClassRestrictionBuilderInterface

    val tailsetSize: Int


    fun forEachConcept(action: (SingleClassLoopTableEntryRestriction) -> Unit)

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false)

}