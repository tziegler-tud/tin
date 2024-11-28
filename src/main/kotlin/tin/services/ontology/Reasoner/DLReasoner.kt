package tin.services.ontology.Reasoner

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet
import tin.services.ontology.Expressions.DLExpression

interface DLReasoner {

    fun checkIsSubsumed(expr: DLExpression, superExpression: DLExpression): Boolean

    fun checkPropertySubsumption(property: OWLObjectPropertyExpression, superProperty: OWLObjectPropertyExpression): Boolean

    fun calculateSuperProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression>

    fun calculateSuperPropertiesMock(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression>

    fun calculateSubProperties(property: OWLObjectPropertyExpression): NodeSet<OWLObjectPropertyExpression>

    fun calculateSubClasses(expr: DLExpression, includeNothing: Boolean =false): HashSet<OWLClass>

    fun getClasses(individual: OWLNamedIndividual): NodeSet<OWLClass>

    fun getTopClassNode(): Node<OWLClass>

    fun getBottomClassNode(): Node<OWLClass>

    fun calculateSuperClasses(expr: DLExpression, includeEquivalent: Boolean): NodeSet<OWLClass>

    fun calculateEquivalentClasses(expr: DLExpression): Node<OWLClass>

    fun checkIndividualEntailment(owlIndividual: OWLIndividual, expr: DLExpression): Boolean

    fun getStats() : Map<String, Int>

    fun getOWLReasoner() : OWLReasoner

    fun clearCache();
}