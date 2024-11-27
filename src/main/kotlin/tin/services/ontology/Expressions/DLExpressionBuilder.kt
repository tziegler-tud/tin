package tin.services.ontology.Expressions

import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import tin.services.ontology.OntologyManager
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl
import uk.ac.manchester.cs.owl.owlapi.OWLSubObjectPropertyOfAxiomImpl

class DLExpressionBuilder(private val manager: OntologyManager) {
    private val ontology = manager.getOntology();
    private val shortFormProvider = manager.getShortFormProvider();
    private val queryParser = manager.getQueryParser();

    enum class ExpressionTypes {
        ELH, ELHI
    }

    fun create(classExpression: OWLClassExpression, expressionType: ExpressionTypes): DLExpression {
        when(expressionType){
            ExpressionTypes.ELH -> {
                val exp = ELHExpression(classExpression);
                return exp;
            }
            ExpressionTypes.ELHI -> {
                val exp = ELHIExpression(classExpression);
                return exp;
            }
        }
    }

    fun createELHExpression(classExpression: OWLClassExpression): DLExpression {
        return create(classExpression, ExpressionTypes.ELH);
    }

    fun createELHIExpression(classExpression: OWLClassExpression): DLExpression {
        return create(classExpression, ExpressionTypes.ELHI);
    }

    fun createELHIExpression(nodeset: NodeSet<OWLClass>): DLExpression {
        val classes: MutableSet<OWLClass> = mutableSetOf();
        nodeset.forEach { node: Node<OWLClass> ->
            classes.add(node.representativeElement)
        }
        return create(createClassExpression(classes), ExpressionTypes.ELHI);
    }

    fun createELHExpressionFromString(expressionString: String) : DLExpression{
        return createFromString(expressionString, ExpressionTypes.ELH)
    }

    fun createELHIExpressionFromString(expressionString: String) : DLExpression {
        return createFromString(expressionString, ExpressionTypes.ELHI)
    }

    fun createFromString(expressionString: String, expressionType: ExpressionTypes) : DLExpression {
        val parsed = queryParser.parseClassExpression(expressionString);

        return create(parsed, expressionType)
    }

    fun createExistentialRestriction(propertyExpression: OWLObjectPropertyExpression, classExpression: OWLClassExpression) : OWLObjectSomeValuesFrom {
        return OWLObjectSomeValuesFromImpl(propertyExpression, classExpression)
    }

    fun createSubsumptionExpression(expr: DLExpression, superExpr: DLExpression) : OWLSubClassOfAxiom {
        return OWLSubClassOfAxiomImpl(expr.getClassExpression(), superExpr.getClassExpression(), HashSet<OWLAnnotation>())
    }

    fun createPropertySubsumptionExpression(property: OWLObjectPropertyExpression, superProperty: OWLObjectPropertyExpression) : OWLSubObjectPropertyOfAxiom {
        return OWLSubObjectPropertyOfAxiomImpl(property, superProperty, HashSet<OWLAnnotation>())
    }

    fun createClassExpression(classSet: Set<OWLClass>): OWLClassExpression {
        return OWLObjectIntersectionOfImpl(classSet.toList());
    }
}