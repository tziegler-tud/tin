package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.model.v2.graph.Node
import tin.services.ontology.DLQueryParser
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl

class RestrictionBuilder(private val queryParser: DLQueryParser, private val shortFormProvider: ShortFormProvider) {

    fun createConceptNameRestriction(values: Set<String>): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        for (value in values) {
            val exp = queryParser.getOWLClass(value);
            if(exp !== null) {
                restriction.addElement(exp);
            }
        }
        return restriction
    }

    fun createConceptNameRestriction(vararg n: String): ConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestriction(values)
    }

    fun createConceptNameRestriction(element: OWLClass): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        restriction.addElement(element);
        return restriction;
    }

//    fun createConceptNameRestriction(elements: Set<OWLClass>): ConceptNameRestriction {
//        val restriction = ConceptNameRestriction();
//        for (element in elements) {
//            restriction.addElement(element);
//        }
//        return restriction;
//    }

    fun asClassExpression(conceptNameRestriction: ConceptNameRestriction) : OWLClassExpression {
        return OWLObjectIntersectionOfImpl(conceptNameRestriction.asList());
    }
}