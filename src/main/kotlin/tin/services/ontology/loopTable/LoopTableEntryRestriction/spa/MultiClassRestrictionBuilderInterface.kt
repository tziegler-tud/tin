package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface

interface MultiClassRestrictionBuilderInterface : RestrictionBuilderInterface<OWLClass> {

    fun createConceptNameRestriction(element: OWLClass): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(values: Set<OWLClass>): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(vararg n: OWLClass): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(vararg n: String): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestrictionFromStringSet(values: Set<String>): MultiClassLoopTableEntryRestriction

    fun asClassExpression(restriction: MultiClassLoopTableEntryRestriction) : OWLClassExpression
}