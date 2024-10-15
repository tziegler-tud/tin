package tin.services.ontology.loopTable.LoopTableEntryRestriction

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.model.v2.graph.Node
import tin.services.ontology.DLQueryParser
import uk.ac.manchester.cs.owl.owlapi.OWLAnonymousClassExpressionImpl
import uk.ac.manchester.cs.owl.owlapi.OWLClassExpressionImpl
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl

class RestrictionBuilder(private val queryParser: DLQueryParser, private val shortFormProvider: ShortFormProvider) {

    fun createConceptNameRestrictionFromStringSet(values: Set<String>): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        for (value in values) {
            val exp = queryParser.getOWLClass(value);
            if(exp !== null) {
                restriction.addElement(exp);
            }
        }
        return restriction
    }

    fun createConceptNameRestriction(values: Set<OWLClass>): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        for (value in values) {
            restriction.addElement(value);
        }
        return restriction
    }

    fun createConceptNameRestriction(vararg n: OWLClass): ConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestriction(values)
    }


    fun createConceptNameRestriction(vararg n: String): ConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestrictionFromStringSet(values)
    }

//    fun createConceptNameRestriction(element: OWLClass): ConceptNameRestriction {
//        val restriction = ConceptNameRestriction();
//        restriction.addElement(element);
//        return restriction;
//    }

//    fun createConceptNameRestriction(elements: Set<OWLClass>): ConceptNameRestriction {
//        val restriction = ConceptNameRestriction();
//        for (element in elements) {
//            restriction.addElement(element);
//        }
//        return restriction;
//    }

    fun asClassExpression(conceptNameRestriction: ConceptNameRestriction) : OWLClassExpression {
        if(conceptNameRestriction.isEmpty()) {
            throw Error("Cannot create class Expression from empty restriction.")
        }
        if(conceptNameRestriction.value.size == 1) {
            return conceptNameRestriction.value.first();
        }
        return OWLObjectIntersectionOfImpl(conceptNameRestriction.asList());
    }

    fun testUnion(conceptNameRestriction: ConceptNameRestriction) : OWLClassExpression {
        return OWLObjectUnionOfImpl(conceptNameRestriction.asList());
    }

    fun testNoIntersect(conceptNameRestriction: ConceptNameRestriction): OWLClassExpression {
        return conceptNameRestriction.asList().first();
    }
}