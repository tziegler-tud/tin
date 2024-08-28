package tin.services.ontology

import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.expression.ShortFormEntityChecker
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter
import org.semanticweb.owlapi.util.ShortFormProvider
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl

class DLQueryParser(private val ontology: OWLOntology, shortFormProvider: ShortFormProvider) {
    private val bidiShortFormProvider: BidirectionalShortFormProvider

    init {
        val manager = ontology.owlOntologyManager
        val importsClosure = ontology.importsClosure
        // Create a bidirectional short form provider to do the actual mapping.
        // It will generate names using the input
        // short form provider.
        bidiShortFormProvider = BidirectionalShortFormProviderAdapter(
            manager,
            importsClosure, shortFormProvider
        )
    }

    fun fromClassNames(classNames: Set<String>) : OWLClassExpression {
        val classes: MutableList<OWLClassExpression> = mutableListOf<OWLClassExpression>()
        classNames.forEach { className ->
            val c = getOWLClass(className);
            if (c != null) classes.add(c);
        }
        return OWLObjectIntersectionOfImpl(classes)
    }

    fun parseClassExpression(classExpressionString: String): OWLClassExpression {
        val dataFactory = ontology.owlOntologyManager
            .owlDataFactory
        val entityChecker: OWLEntityChecker = ShortFormEntityChecker(bidiShortFormProvider)
        val parser = ManchesterOWLSyntaxClassExpressionParser(dataFactory, entityChecker);
        return parser.parse(classExpressionString)
    }

    fun getNamedIndividual(individualName: String): OWLNamedIndividual? {
        val entity = bidiShortFormProvider.getEntity(individualName);
        if (entity != null) {
            if (entity.isIndividual) return entity.asOWLNamedIndividual();
        }
        return null;
    }

    fun getOWLClass(className: String): OWLClass? {
        val entity = bidiShortFormProvider.getEntity(className);
        if (entity != null) {
            if (entity.isOWLClass) return entity.asOWLClass();
        }
        return null;
    }
}