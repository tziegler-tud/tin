package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import tin.services.ontology.DLQueryParser
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl
import kotlin.math.pow

/**
 * Builds restrictions using long ints to represent OWLClasses
 */
class NumericRestrictionBuilder(
    private val classes: Set<OWLClass>,
    private val queryParser: DLQueryParser
) : MultiClassRestrictionBuilderInterface
{
    /**
     * 
     */
    private val maxNumber: Long = 2.0.pow(classes.size.toDouble()).toLong();

    private val classIndexMap: HashMap<Int, OWLClass> = hashMapOf()
    private val classIndexList: List<OWLClass> = classes.toList();

    private val numbericSetUtility: NumericSetUtility = NumericSetUtility(classIndexList);

    init {
        classIndexList.forEachIndexed { index, owlClass ->
            classIndexMap[index] = owlClass;
        }
    }

    override fun createRestriction(element: OWLClass): LoopTableEntryRestriction {
        return createConceptNameRestriction(element);
    }

    fun createConceptNameRestriction(base: ULong) : NumericConceptNameRestriction {
        return NumericConceptNameRestriction(numbericSetUtility, base);
    }

    override fun createConceptNameRestriction(element: OWLClass): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        restriction.addElement(element);
        return restriction;
    }

    override fun createConceptNameRestriction(values: Set<OWLClass>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        for (value in values) {
            restriction.addElement(value);
        }
        return restriction
    }

    override fun createConceptNameRestrictionFromEntities(values: Set<OWLEntity>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        for (value in values) {
            restriction.addElement(value.asOWLClass());
        }
        return restriction
    }

    override fun createConceptNameRestriction(vararg n: OWLClass): NumericConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestriction(values)
    }


    override fun createConceptNameRestriction(vararg n: String): NumericConceptNameRestriction {
        val values = hashSetOf(*n);
        return createConceptNameRestrictionFromStringSet(values)
    }

    override fun createConceptNameRestrictionFromStringSet(values: Set<String>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numbericSetUtility);
        for (value in values) {
            val exp = queryParser.getOWLClass(value);
            if(exp !== null) {
                restriction.addElement(exp);
            }
        }
        return restriction
    }

    fun asClassExpression(restriction: NumericConceptNameRestriction): OWLClassExpression {
        if(restriction.isEmpty()) {
            throw Error("Cannot create class Expression from empty restriction.")
        }
        return OWLObjectIntersectionOfImpl(restriction.asList());
    }

    override fun asClassExpression(restriction: MultiClassLoopTableEntryRestriction) : OWLClassExpression {
        if(restriction.isEmpty()) {
            throw Error("Cannot create class Expression from empty restriction.")
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