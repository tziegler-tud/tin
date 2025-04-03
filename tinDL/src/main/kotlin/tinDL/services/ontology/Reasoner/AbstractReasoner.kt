package tinDL.services.ontology.Reasoner

import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet
import tinDL.services.ontology.Expressions.DLExpression
import tinDL.services.ontology.Expressions.DLExpressionBuilder
import uk.ac.manchester.cs.owl.owlapi.OWLAxiomImpl
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl
import uk.ac.manchester.cs.owl.owlapi.OWLIndividualAxiomImpl
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl

abstract class AbstractReasoner(
    val reasoner: OWLReasoner,
    val expressionBuilder: DLExpressionBuilder
): DLReasoner {

    open override fun clearCache() {
        return
    }

    open override fun getStats() : Map<String, Int> {
        return mapOf<String, Int>();
    }

    override fun getOWLReasoner(): OWLReasoner {
        return reasoner;
    }

    override fun getClasses(individual: OWLNamedIndividual): NodeSet<OWLClass> {
        return reasoner.getTypes(individual);
    }

    override fun getTopClassNode(): Node<OWLClass> {
        return reasoner.topClassNode;
    }

    override fun getBottomClassNode(): Node<OWLClass> {
        return reasoner.bottomClassNode;
    }

    override fun calculateEquivalentClasses(expr: DLExpression): Node<OWLClass> {
        return reasoner.getEquivalentClasses(expr.getClassExpression());
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

    override fun checkIndividualEntailment(owlIndividual: OWLIndividual, expr: DLExpression): Boolean {
        val axiom = OWLClassAssertionAxiomImpl(owlIndividual, expr.getClassExpression(), HashSet<OWLAnnotation>())
        return reasoner.isEntailed(axiom)
    }

    override fun getConnectedIndividuals(property: OWLObjectPropertyExpression, individual: OWLNamedIndividual): NodeSet<OWLNamedIndividual> {
        return reasoner.getObjectPropertyValues(individual, property);
    }

}