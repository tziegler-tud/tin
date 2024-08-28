package tin.services.ontology.Expressions

import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLPropertyExpression
import tin.services.ontology.DLQueryParser

open class ELHExpression(override val expression: OWLClassExpression): DLExpression {

    override fun isValid():Boolean {
        return true;
    }

    override fun getClassExpression(): OWLClassExpression {
        return expression;
    }

    override fun getClassNames(): HashSet<OWLClassExpression> {
        return HashSet<OWLClassExpression>();
    }

    override fun getRoleNames(): HashSet<OWLPropertyExpression> {
        return HashSet<OWLPropertyExpression>();
    }
}