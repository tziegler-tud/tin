package tinCORE.services.ontology.ResultGraph

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tinDL.model.v2.ResultGraph.DlResultGraph
import tinDL.model.v2.ResultGraph.DlResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinDL.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyManager
import tinCORE.services.internal.fileReaders.*
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService
import tinDL.model.v2.ResultGraph.DlResultGraphIndividualFactory
import tinDL.services.ontology.ResultGraph.ELHIResultGraphBuilder
import java.io.File

@SpringBootTest
@TestConfiguration
class ELHIResultGraphBuilderTest {

    private val resultGraphTestUtils: tinCORE.services.ontology.ResultGraph.ResultGraphTestUtils =
        tinCORE.services.ontology.ResultGraph.ResultGraphTestUtils();


    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService;

    fun readWithFileReaderService(fileName: String, breakOnError: Boolean = false): FileReaderResult<File> {
        var fileReaderService: OntologyReaderService = OntologyReaderService(systemConfigurationService);
        val testFilePath = systemConfigurationService.getOntologyPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    fun loadExampleOntology(testOntologyFileName: String): OntologyManager {
        val exampleFile = readWithFileReaderService(testOntologyFileName).get()
        val manager = OntologyManager(exampleFile);
        return manager
    }

    private fun readQueryWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<QueryGraph> {
        var fileReaderService: QueryReaderServiceV2 = QueryReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getQueryPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    private fun readTransducerWithFileReaderService(fileName: String, breakOnError: Boolean = false) : FileReaderResult<TransducerGraph> {
        var fileReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(systemConfigurationService);
        val testFilePath = systemConfigurationService.getTransducerPath();
        return fileReaderService.read(testFilePath, fileName, breakOnError);
    }

    @Test
    fun testRestrictedGraphConstruction() {
        val manager = loadExampleOntology("pizza_4.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC);
        val query = readQueryWithFileReaderService("resultGraph/test1.txt")
        val transducer = readTransducerWithFileReaderService("resultGraph/test1.txt")

        val queryGraph = query.graph;
        val transducerGraph = transducer.graph;

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val place1 = ec.parser.getNamedIndividual("place1")!!;
        val place2 = ec.parser.getNamedIndividual("place2")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!
        val serves = ec.parser.getOWLObjectProperty("serves")!!
        val serves_drink = ec.parser.getOWLObjectProperty("serves_drink")!!
        val serves_meal = ec.parser.getOWLObjectProperty("serves_meal")!!


        val resultGraphBuilder = ELHIResultGraphBuilder(ec, query.graph, transducer.graph)
        val restrictedGraph = resultGraphBuilder.constructRestrictedGraph();
        val comparisonGraph = resultGraphTestUtils.buildComparisonGraphRestricted(ec, query.graph, transducer.graph);

        assert(restrictedGraph == comparisonGraph)


    }

    @Test
    fun testResultGraphConstruction() {
        val manager = loadExampleOntology("pizza_4.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC);
        val query = readQueryWithFileReaderService("resultGraph/test1.txt")
        val transducer = readTransducerWithFileReaderService("resultGraph/test1.txt")

        val testTable = resultGraphTestUtils.generateTestTableELHI(ec, query.graph, transducer.graph);
        val resultGraphBuilder = ELHIResultGraphBuilder(ec, query.graph, transducer.graph)
        val restrictedGraph = resultGraphBuilder.constructRestrictedGraph();
        val resultGraph = resultGraphBuilder.constructResultGraph(testTable);
        val comparisonGraph = buildComparisonGraph(ec, query.graph, transducer.graph);

        assert(resultGraph == comparisonGraph)
    }

    fun buildComparisonGraph(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : DlResultGraph {
        val comparisonGraph = resultGraphTestUtils.buildComparisonGraphRestricted(ec, queryGraph, transducerGraph);

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val owlBeer = ec.parser.getNamedIndividual("beer")!!;
        val owlVeganPlace = ec.parser.getNamedIndividual("VeganPlace")!!

        val individualFactory = DlResultGraphIndividualFactory(ec.shortFormProvider)
        val beer = individualFactory.fromOWLNamedIndividual(owlBeer)
        val veganPlace = individualFactory.fromOWLNamedIndividual(owlVeganPlace)

        comparisonGraph.addEdge(DlResultNode(s0,t0, beer), DlResultNode(s1,t0,beer), 4)
        comparisonGraph.addEdge(DlResultNode(s0,t1,beer), DlResultNode(s1,t1,beer), 7)
        comparisonGraph.addEdge(DlResultNode(s0,t0,veganPlace), DlResultNode(s1,t0,veganPlace), 13)

        return comparisonGraph;

    }
}