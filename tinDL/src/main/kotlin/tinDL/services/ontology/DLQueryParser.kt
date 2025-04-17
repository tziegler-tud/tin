package tinDL.services.ontology

import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.expression.ShortFormEntityChecker
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter
import org.semanticweb.owlapi.util.ShortFormProvider
import tinLIB.model.v2.graph.EdgeLabelProperty
import tinDL.services.ontology.Expressions.DLExpression
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl

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

    fun getOWLClass(edgeLabelProperty: EdgeLabelProperty): OWLClass? {
        if(!edgeLabelProperty.isConceptAssertion()) return null;
        return getOWLClass(edgeLabelProperty.getLabel())
    }

    fun getOWLObjectProperty(propertyName: String): OWLObjectProperty? {
        val entity = bidiShortFormProvider.getEntity(propertyName);
        if (entity != null) {
            if (entity.isOWLObjectProperty) return entity.asOWLObjectProperty();
        }
        return null;
    }

    fun getOWLObjectPropertyExpression(edgeLabelProperty: EdgeLabelProperty): OWLObjectPropertyExpression? {
        val entity = bidiShortFormProvider.getEntity(edgeLabelProperty.getLabel());
        if (entity != null) {
            if (entity.isOWLObjectProperty) {
                return if (edgeLabelProperty.isInverse()) {
                    entity.asOWLObjectProperty().inverseProperty
                } else {
                    entity.asOWLObjectProperty();
                }
            };
        }
        return null;
    }

    fun getTopOWLClass(): OWLClass {
        return getOWLClass("owl:Thing")!!;
    }

    fun getBottomOWLClass(): OWLClass {
        return getOWLClass("owl:Nothing")!!;
    }

    fun getTopOWLProperty() : OWLObjectProperty {
        return getOWLObjectProperty("owl:topObjectProperty")!!;
    }
}