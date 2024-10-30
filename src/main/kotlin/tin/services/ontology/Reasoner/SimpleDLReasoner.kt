package tin.services.ontology.Reasoner

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet
import tin.services.ontology.Expressions.DLExpression
import tin.services.ontology.Expressions.DLExpressionBuilder
import kotlin.streams.toList

/**
 * wrapper class to provide some high-level utility using the owl reasoner interface
 */
class SimpleDLReasoner(
    val reasoner: OWLReasoner,
    private val expressionBuilder: DLExpressionBuilder
) : AbstractReasoner() {

    override fun checkIsSubsumed(expr: DLExpression, superExpression: DLExpression): Boolean {
        val subex = expressionBuilder.createSubsumptionExpression(expr, superExpression);
        return reasoner.isEntailed(subex);
    }

    override fun checkPropertySubsumption(property: OWLObjectPropertyExpression, superProperty: OWLObjectPropertyExpression): Boolean {
        val subex = expressionBuilder.createPropertySubsumptionExpression(property, superProperty);
        val isEntailed = reasoner.isEntailed(subex);
        return isEntailed;
    }

    override fun calculateSuperProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSuperObjectProperties(property, false);
        return properties;
    }

    override fun calculateSuperPropertiesMock(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        return OWLObjectPropertyNodeSet(property)
    }

    override fun calculateSubProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression> {
        val properties = reasoner.getSubObjectProperties(property, false);
        return properties;
    }

    override fun calculateSubClasses(expr: DLExpression): HashSet<OWLClass> {
        var classes = reasoner.getSubClasses(expr.getClassExpression(), false)
        //remove owl:Nothing
        if(classes.isBottomSingleton) {
            classes = OWLClassNodeSet();
        }
        val hashSet = classes.entities().toList().toHashSet()
        return hashSet;
    }

    override fun getTopClassNode(): Node<OWLClass> {
        return reasoner.topClassNode;
    }

    override fun getBottomClassNode(): Node<OWLClass> {
        return reasoner.bottomClassNode;
    }

    override fun calculateSuperClasses(expr: DLExpression, includeEquivalent: Boolean): NodeSet<OWLClass> {
        val strictSuperClasses: NodeSet<OWLClass> = reasoner.getSuperClasses(expr.getClassExpression());

        if(includeEquivalent) {
            val equivNode: Node<OWLClass> = reasoner.getEquivalentClasses(expr.getClassExpression());
            val setWithEquiv = OWLClassNodeSet(strictSuperClasses.nodes)
            setWithEquiv.addNode(equivNode);
            return setWithEquiv;
        }
        return strictSuperClasses;
    }

    override fun calculateEquivalentClasses(expr: DLExpression): Node<OWLClass> {
        return reasoner.getEquivalentClasses(expr.getClassExpression());
    }
}