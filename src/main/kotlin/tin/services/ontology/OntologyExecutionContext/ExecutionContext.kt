package tin.services.ontology.OntologyExecutionContext

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
import tin.services.ontology.OntologyManager
import tin.services.ontology.Reasoner.DLReasoner
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface

interface ExecutionContext {

    val dlReasoner: DLReasoner
    val expressionBuilder: DLExpressionBuilder
    val parser: DLQueryParser
    val shortFormProvider: ShortFormProvider
    val manchesterShortFormProvider: ManchesterOWLSyntaxPrefixNameShortFormProvider
    val spaRestrictionBuilder: RestrictionBuilderInterface<OWLClass>
    val spRestrictionBuilder: RestrictionBuilderInterface<OWLIndividual>

    fun prewarmSubsumptionCache();

    fun getClasses(): Set<OWLClass>

    fun getClassAmount(): Int

    fun getClassNames(): HashSet<String>

    fun getRoleNames(): HashSet<String>

    fun getRoles(): Set<OWLObjectProperty>

    fun getManager(): OntologyManager
}