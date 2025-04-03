package tin.services.ontology.loopTable.LoopTableEntryRestriction.spa

import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.util.ShortFormProvider
import tin.services.ontology.DLQueryParser
import tin.services.ontology.loopTable.LoopTableEntryRestriction.LoopTableEntryRestriction
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl

class SingleClassRestrictionBuilder(
    private val queryParser: DLQueryParser,
    ) : SingleClassRestrictionBuilderInterface {

    override fun createConceptNameRestriction(element: OWLClass): SingleClassLoopTableEntryRestriction {
        return SingleClassConceptNameRestriction(element)
    }

    override fun createConceptNameRestriction(className: String): SingleClassLoopTableEntryRestriction {
        val exp = queryParser.getOWLClass(className);
        if(exp !== null) {
            return SingleClassConceptNameRestriction(exp);
        }
        else {
            throw Error("Unable to create Restriction from String: className '${className}' not found in Ontology.")
        }
    }

    override fun asClassExpression(restriction: SingleClassLoopTableEntryRestriction) : OWLClassExpression {
        return restriction.value
    }

}