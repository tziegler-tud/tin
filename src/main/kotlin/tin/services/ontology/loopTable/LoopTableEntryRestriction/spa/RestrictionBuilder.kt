package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.services.ontology.DLQueryParser
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl

class RestrictionBuilder(
    private val queryParser: DLQueryParser,
    private val shortFormProvider: ShortFormProvider
) : MultiClassRestrictionBuilderInterface {

    override fun createConceptNameRestrictionFromStringSet(values: Set<String>): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        for (value in values) {
            val exp = queryParser.getOWLClass(value);
            if(exp !== null) {
                restriction.addElement(exp);
            }
        }
        return restriction
    }

    override fun createConceptNameRestriction(element: OWLClass): ConceptNameRestriction {
        val restriction = ConceptNameRestriction()
        restriction.addElement(element);
        return restriction;
    }

    override fun createConceptNameRestriction(values: Set<OWLClass>): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        for (value in values) {
            restriction.addElement(value);
        }
        return restriction
    }

    override fun createConceptNameRestrictionFromEntities(values: Set<OWLEntity>): ConceptNameRestriction {
        val restriction = ConceptNameRestriction();
        for (value in values) {
            restriction.addElement(value.asOWLClass());
        }
        return restriction
    }

    override fun createConceptNameRestriction(vararg n: OWLClass): ConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestriction(values)
    }


    override fun createConceptNameRestriction(vararg n: String): ConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestrictionFromStringSet(values)
    }

    override fun createConceptNameRestriction(nodeset: NodeSet<OWLClass>): MultiClassLoopTableEntryRestriction {
        val classes: MutableSet<OWLClass> = mutableSetOf();
        nodeset.forEach { node: Node<OWLClass> ->
            classes.add(node.representativeElement)
        }
        return createConceptNameRestriction(classes)
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

    override fun asClassExpression(restriction: MultiClassLoopTableEntryRestriction) : OWLClassExpression {
        if(restriction.isEmpty()) {
            throw Error("Cannot create class Expression from empty restriction.")
        }
        val set = restriction.asSet()
        if(set.size == 1) {
            return set.first();
        }
        return OWLObjectIntersectionOfImpl(restriction.asList());
    }


    fun testUnion(conceptNameRestriction: ConceptNameRestriction) : OWLClassExpression {
        return OWLObjectUnionOfImpl(conceptNameRestriction.asList());
    }

    fun testNoIntersect(conceptNameRestriction: ConceptNameRestriction): OWLClassExpression {
        return conceptNameRestriction.asList().first();
    }
}