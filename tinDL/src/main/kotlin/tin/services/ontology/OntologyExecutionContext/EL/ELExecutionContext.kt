package tin.services.ontology.OntologyExecutionContext.EL

import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.*

interface ELExecutionContext : ExecutionContext {
    override val spaRestrictionBuilder: SingleClassRestrictionBuilderInterface
    override val spRestrictionBuilder: SingleClassRestrictionBuilderInterface

    val tailsetSize: Int


    fun forEachConcept(action: (SingleClassLoopTableEntryRestriction) -> Unit)

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false)

}