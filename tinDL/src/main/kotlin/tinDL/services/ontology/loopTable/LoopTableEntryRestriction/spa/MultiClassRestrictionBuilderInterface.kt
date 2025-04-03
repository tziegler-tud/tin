package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.reasoner.NodeSet
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.RestrictionBuilderInterface

interface MultiClassRestrictionBuilderInterface : RestrictionBuilderInterface {

    fun createConceptNameRestriction(element: OWLClass): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(values: Set<OWLClass>): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestrictionFromEntities(values: Set<OWLEntity>): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(vararg n: OWLClass): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(vararg n: String): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestriction(nodeset: NodeSet<OWLClass>): MultiClassLoopTableEntryRestriction

    fun createConceptNameRestrictionFromStringSet(values: Set<String>): MultiClassLoopTableEntryRestriction

    fun asClassExpression(restriction: MultiClassLoopTableEntryRestriction) : OWLClassExpression
}