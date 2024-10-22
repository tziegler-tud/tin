package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl

interface RestrictionBuilderInterface {

    fun createConceptNameRestriction(element: OWLClass): LoopTableEntryRestriction<OWLClass>

    fun createConceptNameRestriction(values: Set<OWLClass>): LoopTableEntryRestriction<OWLClass>

    fun createConceptNameRestriction(vararg n: OWLClass): LoopTableEntryRestriction<OWLClass>

    fun createConceptNameRestriction(vararg n: String): LoopTableEntryRestriction<OWLClass>

    fun createConceptNameRestrictionFromStringSet(values: Set<String>): LoopTableEntryRestriction<OWLClass>

    fun asClassExpression(restriction: LoopTableEntryRestriction<OWLClass>) : OWLClassExpression
}