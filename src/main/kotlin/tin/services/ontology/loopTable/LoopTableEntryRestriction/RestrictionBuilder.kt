package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.services.ontology.DLQueryParser

class RestrictionBuilder(private val queryParser: DLQueryParser, private val shortFormProvider: ShortFormProvider) {

    fun createConceptNameRestriction(value: HashSet<String>): ConceptNameRestriction {
        return ConceptNameRestriction(value)
    }

    fun asClassExpression(conceptNameRestriction: ConceptNameRestriction) : OWLClassExpression {
        return queryParser.fromClassNames(conceptNameRestriction.value);
    }

}