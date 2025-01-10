package tin.services.ontology.OntologyExecutionContext

import org.apache.el.lang.ExpressionBuilder
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.services.ontology.DLQueryParser
import tin.services.ontology.Expressions.DLExpressionBuilder
import tin.services.ontology.OntologyManager
import tin.services.ontology.Reasoner.DLReasoner
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface
import tin.services.ontology.loopTable.LoopTableEntryRestriction.spa.MultiClassLoopTableEntryRestriction

interface ExecutionContext {

    val dlReasoner: DLReasoner
    val resultGraphReasoner: DLReasoner
    val expressionBuilder: DLExpressionBuilder
    val parser: DLQueryParser
    val shortFormProvider: ShortFormProvider
    val manchesterShortFormProvider: ManchesterOWLSyntaxPrefixNameShortFormProvider
    val spaRestrictionBuilder: RestrictionBuilderInterface
    val spRestrictionBuilder: RestrictionBuilderInterface

    val individuals: Set<OWLNamedIndividual>

    fun prewarmSubsumptionCache();

    fun getClasses(): Set<OWLClass>

    fun getClassAmount(): Int

    fun getClassNames(): HashSet<String>

    fun getRoleNames(): HashSet<String>

    fun getRoles(): Set<OWLObjectProperty>

    fun getManager(): OntologyManager

    fun forEachIndividual(action: (OWLNamedIndividual) -> Unit)

}