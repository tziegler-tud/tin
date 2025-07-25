package tinDL.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import tinDL.services.ontology.DLQueryParser
import tinDL.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl
import kotlin.math.pow

/**
 * Builds restrictions using long ints to represent OWLClasses
 */
class NumericRestrictionBuilder(
    private val topClass: OWLClass,
    private val bottomClass: OWLClass,
    private val classes: Set<OWLClass>,
    private val queryParser: DLQueryParser
) : MultiClassRestrictionBuilderInterface
{
    /**
     * 
     */
    private val maxNumber: Long = 2.0.pow(classes.size.toDouble()).toLong();

    private val classIndexMap: HashMap<Int, OWLClass> = hashMapOf()
    private val classIndexList: MutableList<OWLClass> = classes.toList().toMutableList()

    private lateinit var numericSetUtility: NumericSetUtility;

    init {
        classIndexList.add(topClass);
        classIndexList.add(bottomClass);
        classIndexList.forEachIndexed { index, owlClass ->
            classIndexMap[index] = owlClass;
        }
        numericSetUtility = NumericSetUtility(classIndexList);
    }

    fun createConceptNameRestriction(base: ULong) : NumericConceptNameRestriction {
        return NumericConceptNameRestriction(numericSetUtility, base);
    }

    override fun createConceptNameRestriction(element: OWLClass): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numericSetUtility);
        restriction.addElement(element);
        return restriction;
    }

    override fun createConceptNameRestriction(values: Set<OWLClass>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numericSetUtility);
        for (value in values) {
            restriction.addElement(value);
        }
        return restriction
    }

    override fun createConceptNameRestrictionFromEntities(values: Set<OWLEntity>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numericSetUtility);
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

    override fun createConceptNameRestriction(nodeset: NodeSet<OWLClass>): MultiClassLoopTableEntryRestriction {
        val classes: MutableSet<OWLClass> = mutableSetOf();
        nodeset.forEach { node: Node<OWLClass> ->
            classes.add(node.representativeElement)
        }
        return createConceptNameRestriction(classes)
    }

    override fun createConceptNameRestrictionFromStringSet(values: Set<String>): NumericConceptNameRestriction {
        val restriction = NumericConceptNameRestriction(numericSetUtility);
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
        if(restriction.getSize() == 1) return restriction.asList().first();
        return OWLObjectIntersectionOfImpl(restriction.asList());
    }

    override fun asClassExpression(restriction: MultiClassLoopTableEntryRestriction) : OWLClassExpression {
        if(restriction.isEmpty()) {
            throw Error("Cannot create class Expression from empty restriction.")
        }
        if(restriction.getSize() == 1) return restriction.asList().first();
        return OWLObjectIntersectionOfImpl(restriction.asList());
    }

    fun getAllDirectSubsets(restriction: NumericConceptNameRestriction): Set<NumericConceptNameRestriction> {
        val result: MutableSet<NumericConceptNameRestriction> = mutableSetOf()
        val classes = restriction.asSet();
        for(owlClass in classes) {
            val new = NumericConceptNameRestriction(restriction);
            new.removeElement(owlClass)
            result.add(new);
        }
        return result;
    }
}