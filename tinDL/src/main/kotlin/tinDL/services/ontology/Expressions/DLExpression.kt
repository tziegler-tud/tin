package tinDL.services.ontology.Expressions

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLProperty
import org.semanticweb.owlapi.model.OWLPropertyExpression
import tinDL.services.ontology.DLQueryParser

interface DLExpression {

    val expression: OWLClassExpression;

    public fun isValid(): Boolean

    public fun getClassExpression(): OWLClassExpression;

    public fun getClassNames(): HashSet<OWLClassExpression>

    public fun getRoleNames(): HashSet<OWLPropertyExpression>
}