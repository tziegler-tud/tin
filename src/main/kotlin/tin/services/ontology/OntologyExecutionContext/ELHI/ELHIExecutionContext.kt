package tin.services.ontology.OntologyExecutionContext.ELHI

import org.apache.el.lang.ExpressionBuilder
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.services.ontology.DLQueryParser
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.Reasoner.DLReasoner
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassRestrictionBuilderInterface

interface ELHIExecutionContext : ExecutionContext {
    override val spaRestrictionBuilder: MultiClassRestrictionBuilderInterface
    override val spRestrictionBuilder: RestrictionBuilderInterface<OWLIndividual>

    val tailsetSize: ULong

    fun forEachTailset(action: (MultiClassLoopTableEntryRestriction) -> Unit)

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false)
}