package tin.services.ontology.loopTable.LoopTableEntryRestriction

import com.google.common.collect.ImmutableList
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.model.v2.graph.Node
import tin.services.ontology.DLQueryParser
import tin.services.ontology.OntologyManager
import uk.ac.manchester.cs.owl.owlapi.OWLAnonymousClassExpressionImpl
import uk.ac.manchester.cs.owl.owlapi.OWLClassExpressionImpl
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl
import kotlin.math.pow

/**
 * Builds restrictions using long ints to represent OWLClasses
 */
class NumericRestrictionBuilder(private val manager: OntologyManager, private val queryParser: DLQueryParser, private val shortFormProvider: ShortFormProvider) {
    /**
     * 
     */
    private val maxNumber: Long = 2.0.pow(manager.classes.size.toDouble()).toLong();
    private val classes = manager.classes;

    private val classIndexMap: HashMap<Int, OWLClass> = hashMapOf()
    private val classIndexList: List<OWLClass> = classes.toList();

    private val numbericSetUtility: NumericSetUtility = NumericSetUtility(classIndexList);

    init {
        classIndexList.forEachIndexed { index, owlClass ->
            classIndexMap[index] = owlClass;
        }
    }

    fun createConceptNameRestriction(element: OWLClass): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        restriction.addElement(element);
        return restriction;
    }

    fun createConceptNameRestriction(values: Set<OWLClass>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        for (value in values) {
            restriction.addElement(value);
        }
        return restriction
    }

    fun createConceptNameRestriction(vararg n: OWLClass): NumericConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestriction(values)
    }


    fun createConceptNameRestriction(vararg n: String): NumericConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestrictionFromStringSet(values)
    }

    fun createConceptNameRestrictionFromStringSet(values: Set<String>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        for (value in values) {
            val exp = queryParser.getOWLClass(value);
            if(exp !== null) {
                restriction.addElement(exp);
            }
        }
        return restriction
    }

    fun asClassExpression(conceptNameRestriction: NumericConceptNameRestriction) : OWLClassExpression {
        if(conceptNameRestriction.isEmpty()) {
            throw Error("Cannot create class Expression from empty restriction.")
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