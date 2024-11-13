package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import tin.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface

interface SingleClassRestrictionBuilderInterface : RestrictionBuilderInterface<OWLClass> {

    fun createConceptNameRestriction(element: OWLClass): SingleClassLoopTableEntryRestriction

    fun createConceptNameRestriction(className: String): SingleClassLoopTableEntryRestriction

    fun asClassExpression(restriction: SingleClassLoopTableEntryRestriction) : OWLClassExpression

}