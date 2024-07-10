import java.io.BufferedReader
import java.io.InputStreamReader
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.expression.ParserException
import org.semanticweb.owlapi.expression.ShortFormEntityChecker
import org.semanticweb.owlapi.io.StringDocumentSource
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.Node
import org.semanticweb.owlapi.reasoner.NodeSet
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter
import org.semanticweb.owlapi.util.ShortFormProvider
import org.semanticweb.owlapi.util.SimpleShortFormProvider

class ExampleReasoner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Load an example ontology.
            val manager = OWLManager.createOWLOntologyManager()
            val ontology = manager.loadOntologyFromOntologyDocument(StringDocumentSource(koala))
            // We need a reasoner to do our query answering

            // These two lines are the only relevant difference between this code and the original example
            // This example uses HermiT: http://hermit-reasoner.com/
            // We don't want HermiT to thrown an exception for inconsistent ontologies because then we
            // can't explain the inconsistency. This can be controlled via a configuration setting.
            val configuration : Configuration = Configuration();
            configuration.throwInconsistentOntologyException=false;
            val reasoner = ReasonerFactory().createReasoner(ontology)

            val shortFormProvider: ShortFormProvider = SimpleShortFormProvider()
            // Create the DLQueryPrinter helper class. This will manage the
            // parsing of input and printing of results
            val dlQueryPrinter = DLQueryPrinter(DLQueryEngine(reasoner, shortFormProvider), shortFormProvider)
            // Enter the query loop. A user is expected to enter class
            // expression on the command line.
            val br = BufferedReader(InputStreamReader(System.`in`, "UTF-8"))
            while (true) {
                println("Type a class expression in Manchester Syntax and press Enter (or press x to exit):")
                val classExpression = br.readLine()
                // Check for exit condition
                if (classExpression == null || classExpression.equals("x", ignoreCase = true)) {
                    break
                }
                dlQueryPrinter.askQuery(classExpression.trim())
                println()
            }
        }

        // for convenience, the Koala ontology is stored in this string
        private const val koala = """<?xml version=\"1.0\"?>
            <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns=\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#\" xml:base=\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl\">
              <owl:Ontology rdf:about=\"\"/>
              <owl:Class rdf:ID=\"Female\"><owl:equivalentClass><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty><owl:hasValue><Gender rdf:ID=\"female\"/></owl:hasValue></owl:Restriction></owl:equivalentClass></owl:Class>
              <owl:Class rdf:ID=\"Marsupials\"><owl:disjointWith><owl:Class rdf:about=\"#Person\"/></owl:disjointWith><rdfs:subClassOf><owl:Class rdf:about=\"#Animal\"/></rdfs:subClassOf></owl:Class>
              <owl:Class rdf:ID=\"Student\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Class rdf:about=\"#Person\"/><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\"#isHardWorking\"/></owl:onProperty><owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</owl:hasValue></owl:Restriction><owl:Restriction><owl:someValuesFrom><owl:Class rdf:about=\"#University\"/></owl:someValuesFrom><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasHabitat\"/></owl:onProperty></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>
              <owl:Class rdf:ID=\"KoalaWithPhD\"><owl:versionInfo>1.2</owl:versionInfo><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Restriction><owl:hasValue><Degree rdf:ID=\"PhD\"/></owl:hasValue><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasDegree\"/></owl:onProperty></owl:Restriction><owl:Class rdf:about=\"#Koala\"/></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>
              <owl:Class rdf:ID=\"University\"><rdfs:subClassOf><owl:Class rdf:ID=\"Habitat\"/></rdfs:subClassOf></owl:Class>
              <owl:Class rdf:ID=\"Koala\"><rdfs:subClassOf><owl:Restriction><owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">false</owl:hasValue><owl:onProperty><owl:FunctionalProperty rdf:about=\"#isHardWorking\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf><owl:Restriction><owl:someValuesFrom><owl:Class rdf:about=\"#DryEucalyptForest\"/></owl:someValuesFrom><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasHabitat\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\"#Marsupials\"/></owl:Class>
              <owl:Class rdf:ID=\"Animal\"><rdfs:seeAlso>Male</rdfs:seeAlso><rdfs:subClassOf><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasHabitat\"/></owl:onProperty><owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</owl:minCardinality></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf><owl:Restriction><owl:cardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</owl:cardinality><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><owl:versionInfo>1.1</owl:versionInfo></owl:Class>
              <owl:Class rdf:ID=\"Forest\"><rdfs:subClassOf rdf:resource=\"#Habitat\"/></owl:Class>
              <owl:Class rdf:ID=\"Rainforest\"><rdfs:subClassOf rdf:resource=\"#Forest\"/></owl:Class>
              <owl:Class rdf:ID=\"GraduateStudent\"><rdfs:subClassOf><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasDegree\"/></owl:onProperty><owl:someValuesFrom><owl:Class><owl:oneOf rdf:parseType=\"Collection\"><Degree rdf:ID=\"BA\"/><Degree rdf:ID=\"BS\"/></owl:oneOf></owl:Class></owl:someValuesFrom></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\"#Student\"/></owl:Class>
              <owl:Class rdf:ID=\"Parent\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Class rdf:about=\"#Animal\"/><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasChildren\"/></owl:onProperty><owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">1</owl:minCardinality></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass><rdfs:subClassOf rdf:resource=\"#Animal\"/></owl:Class>
              <owl:Class rdf:ID=\"DryEucalyptForest\"><rdfs:subClassOf rdf:resource=\"#Forest\"/></owl:Class>
              <owl:Class rdf:ID=\"Quokka\"><rdfs:subClassOf><owl:Restriction><owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</owl:hasValue><owl:onProperty><owl:FunctionalProperty rdf:about=\"#isHardWorking\"/></owl:onProperty></owl:Restriction></rdfs:subClassOf><rdfs:subClassOf rdf:resource=\"#Marsupials\"/></owl:Class>
              <owl:Class rdf:ID=\"TasmanianDevil\"><rdfs:subClassOf rdf:resource=\"#Marsupials\"/></owl:Class>
              <owl:Class rdf:ID=\"MaleStudentWith3Daughters\"><owl:equivalentClass><owl:Class><owl:intersectionOf rdf:parseType=\"Collection\"><owl:Class rdf:about=\"#Student\"/><owl:Restriction><owl:onProperty><owl:FunctionalProperty rdf:about=\"#hasGender\"/></owl:onProperty><owl:hasValue

><Gender rdf:ID=\"male\"/></owl:hasValue></owl:Restriction><owl:Restriction><owl:onProperty><owl:ObjectProperty rdf:about=\"#hasChildren\"/></owl:onProperty><owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">3</owl:minCardinality><owl:allValuesFrom rdf:resource=\"#Female\"/></owl:Restriction></owl:intersectionOf></owl:Class></owl:equivalentClass></owl:Class>
              <owl:Class rdf:ID=\"Degree\"/>
              <owl:Class rdf:ID=\"Habitat\"/>
              <owl:Class rdf:ID=\"Person\"><rdfs:subClassOf rdf:resource=\"#Animal\"/></owl:Class>
              <owl:Class rdf:ID=\"Gender\"/>
              <owl:Class rdf:ID=\"Male\"><owl:equivalentClass><owl:Restriction><owl:onProperty rdf:resource=\"#hasGender\"/><owl:hasValue rdf:resource=\"#male\"/></owl:Restriction></owl:equivalentClass></owl:Class>
              <owl:ObjectProperty rdf:ID=\"hasDegree\"/>
              <owl:ObjectProperty rdf:ID=\"hasHabitat\"/>
              <owl:ObjectProperty rdf:ID=\"hasChildren\"/>
              <owl:FunctionalProperty rdf:ID=\"hasGender\"/>
              <Female rdf:ID=\"female\"/>
              <Male rdf:ID=\"male\"/>
              <Degree rdf:ID=\"MA\"/>
            </rdf:RDF>"""
    }

    class DLQueryEngine(val reasoner: OWLReasoner, shortFormProvider: ShortFormProvider) {
        private val bidirectionalShortFormProvider: BidirectionalShortFormProvider

        init {
            val manager = reasoner.rootOntology.OWLOntologyManager
            val importsClosure = reasoner.rootOntology.importsClosure
            bidirectionalShortFormProvider = BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider)
        }

        fun getSubClasses(classExpressionString: String?, direct: Boolean): Set<OWLClass> {
            if (classExpressionString.isNullOrEmpty()) return emptySet()
            val classExpression = parseClassExpression(classExpressionString)
            val subClasses: NodeSet<OWLClass> = reasoner.getSubClasses(classExpression, direct)
            return subClasses.flatMapTo(mutableSetOf()) { it.entities }
        }

        fun getSuperClasses(classExpressionString: String?, direct: Boolean): Set<OWLClass> {
            if (classExpressionString.isNullOrEmpty()) return emptySet()
            val classExpression = parseClassExpression(classExpressionString)
            val superClasses: NodeSet<OWLClass> = reasoner.getSuperClasses(classExpression, direct)
            return superClasses.flatMapTo(mutableSetOf()) { it.entities }
        }

        fun getEquivalentClasses(classExpressionString: String?): Set<OWLClass> {
            if (classExpressionString.isNullOrEmpty()) return emptySet()
            val classExpression = parseClassExpression(classExpressionString)
            val equivalentClasses: Node<OWLClass> = reasoner.getEquivalentClasses(classExpression)
            if (classExpression.isAnonymous) {
                return equivalentClasses.flatMapTo(mutableSetOf()) { it.entities }
            }
            return equivalentClasses.entities.minus(classExpression.asOWLClass())
        }

        fun getInstances(classExpressionString: String?, direct: Boolean): Set<OWLNamedIndividual> {
            if (classExpressionString.isNullOrEmpty()) return emptySet()
            val classExpression = parseClassExpression(classExpressionString)
            val instances: NodeSet<OWLNamedIndividual> = reasoner.getInstances(classExpression, direct)
            return instances.flatMapTo(mutableSetOf()) { it.entities }
        }

        private fun parseClassExpression(classExpressionString: String): OWLClassExpression {
            val parser = ManchesterOWLSyntaxEditorParser(OWLManager.getOWLDataFactory(), classExpressionString)
            parser.setDefaultOntology(reasoner.rootOntology)
            val checker: OWLEntityChecker = ShortFormEntityChecker(bidirectionalShortFormProvider)
            parser.entityChecker = checker
            return parser.parseClassExpression()
        }
    }

    class DLQueryPrinter(private val dlQueryEngine: DLQueryEngine, private val shortFormProvider: ShortFormProvider) {
        fun askQuery(classExpression: String) {
            println("QUERY: $classExpression")
            doQuery("SubClasses", classExpression) { expr -> dlQueryEngine.getSubClasses(expr, true) }
            doQuery("EquivalentClasses", classExpression) { expr -> dlQueryEngine.getEquivalentClasses(expr) }
            doQuery("SuperClasses", classExpression) { expr -> dlQueryEngine.getSuperClasses(expr, true) }
            doQuery("Instances", classExpression) { expr -> dlQueryEngine.getInstances(expr, true) }
        }

        private fun doQuery(
            name: String,
            classExpression: String,
            queryFunc: (String) -> Set<out OWLObject>
        ) {
            println("$name")
            val result = try {
                queryFunc(classExpression)
            } catch (e: ParserException) {
                println("PARSE ERROR: ${e.message}")
                return
            }
            if (result.isEmpty()) {
                println("\t[NONE]")
            } else {
                result.forEach { println("\t${shortFormProvider.getShortForm(it)}") }
            }
        }
    }
}