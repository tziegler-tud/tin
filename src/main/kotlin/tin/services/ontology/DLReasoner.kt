package tin.services.ontology

import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder

/**
 * wrapper class to provide some high-level utility using the owl reasoner interface
 */
class DLReasoner(private val reasoner: OWLReasoner, private val expressionBuilder: DLExpressionBuilder) {

    public fun checkIsSubsumed(expr: DLExpression, superExpression: DLExpression): Boolean {
        val subex = expressionBuilder.createSubsumptionExpression(expr, superExpression);
        return reasoner.isEntailed(subex);
    }

    public fun calculateSuperProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSuperObjectProperties(property, false);
        return properties;
    }

    public fun calculateSubProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSubObjectProperties(property, false);
        return properties;
    }

}