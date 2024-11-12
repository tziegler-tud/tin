package tin.services.ontology.OntologyExecutionContext.EL

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Single
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
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassLoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.SingleClassRestrictionBuilderInterface

interface ELExecutionContext : ExecutionContext {
    override val spaRestrictionBuilder: SingleClassRestrictionBuilderInterface
    override val spRestrictionBuilder: RestrictionBuilderInterface<OWLIndividual>

    val tailsetSize: Int


    fun forEachConcept(action: (SingleClassLoopTableEntryRestriction) -> Unit)

    fun prepareForLoopTableConstruction(prewarmCaches: Boolean = false)

}