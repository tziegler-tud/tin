package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression

interface RestrictionBuilderInterface<DataType> {

    fun createRestriction(element: DataType): LoopTableEntryRestriction
}