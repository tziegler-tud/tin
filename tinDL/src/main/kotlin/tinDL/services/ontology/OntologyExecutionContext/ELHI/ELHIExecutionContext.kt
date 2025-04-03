package tinDL.services.ontology.OntologyExecutionContext.ELHI

import org.apache.el.lang.ExpressionBuilder
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.util.ShortFormProvider
import tinDL.services.ontology.DLQueryParser
import tinDL.services.ontology.Expressions.DLExpressionBuilder
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.Reasoner.DLReasoner
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.sp.IndividualRestrictionBuilder
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassRestrictionBuilderInterface

interface ELHIExecutionContext : ExecutionContext {
    override val spaRestrictionBuilder: MultiClassRestrictionBuilderInterface
    override val spRestrictionBuilder: IndividualRestrictionBuilder

    val tailsetSize: ULong

    fun forEachTailset(action: (MultiClassLoopTableEntryRestriction) -> Unit)

    fun forEachTailsetDescending(action: (MultiClassLoopTableEntryRestriction) -> Unit);

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false)

}