package tin.services.ontology.Expressions

import org.semanticweb.owlapi.model.*
import tin.services.ontology.OntologyManager
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl

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
}