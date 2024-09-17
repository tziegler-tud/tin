package tin.services.ontology

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder

/**
 * wrapper class to provide some high-level utility using the owl reasoner interface
 */
class DLReasoner(val reasoner: OWLReasoner, private val expressionBuilder: DLExpressionBuilder) {

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

    fun getTopClassNode(): Node<OWLClass> {
        return reasoner.topClassNode;
    }

    fun getBottomClassNode(): Node<OWLClass> {
        return reasoner.bottomClassNode;
    }

    fun calculateSuperClasses(expr: DLExpression, includeEquivalent: Boolean): NodeSet<OWLClass> {
        val strictSuperClasses = reasoner.getSuperClasses(expr.getClassExpression());
        if(includeEquivalent) {
            val equivNode: Node<OWLClass> = reasoner.getEquivalentClasses(expr.getClassExpression());
            val setWithEquiv = OWLClassNodeSet(strictSuperClasses.nodes)
            setWithEquiv.addNode(equivNode);
            return setWithEquiv;
        }
        return strictSuperClasses;
    }

    fun calculateEquivalentClasses(expr: DLExpression): Node<OWLClass> {
        return reasoner.getEquivalentClasses(expr.getClassExpression());
    }
}