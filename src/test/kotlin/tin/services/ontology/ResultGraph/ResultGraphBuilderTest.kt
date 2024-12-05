package tin.services.ontology.ResultGraph

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.QueryReaderServiceV2
import tin.services.internal.fileReaders.TransducerReaderServiceV2
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHIExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.loopTable.LoopTable.ELHI.ELHISPLoopTable
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File

@SpringBootTest
@TestConfiguration
class ResultGraphBuilderTest {
    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

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
        val comparisonGraph = buildComparisonGraphRestricted(ec, query.graph, transducer.graph);

        assert(restrictedGraph == comparisonGraph)


    }

    @Test
    fun testResultGraphConstruction() {
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


        val testTable = generateTestTable(ec, query.graph, transducer.graph);
        val resultGraphBuilder = ELHIResultGraphBuilder(ec, query.graph, transducer.graph)
        val restrictedGraph = resultGraphBuilder.constructRestrictedGraph();
        val resultGraph = resultGraphBuilder.constructResultGraph(testTable);
        val comparisonGraph = buildComparisonGraph(ec, query.graph, transducer.graph);

        assert(resultGraph == comparisonGraph)
    }

    fun buildComparisonGraph(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ResultGraph {
        val comparisonGraph = buildComparisonGraphRestricted(ec, queryGraph, transducerGraph);

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

        comparisonGraph.addEdge(ResultNode(s0,t0,beer), ResultNode(s1,t0,beer), 4)
        comparisonGraph.addEdge(ResultNode(s0,t1,beer), ResultNode(s1,t1,beer), 7)
        comparisonGraph.addEdge(ResultNode(s0,t0,veganPlace), ResultNode(s1,t0,veganPlace), 13)

        return comparisonGraph;

    }

    fun buildComparisonGraphRestricted(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ResultGraph {
        //build comparison graph
        val comparisonGraph = ResultGraph()

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


        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,beer))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,bruschetta))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,carbonara))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,place1))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,place2))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,r))
                comparisonGraph.addNode(ResultNode(queryNode,transducerNode,veganPlace))
            }
        }

        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s0,t0,beer), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s1,t0,beer), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s0,t0,carbonara), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,place1), ResultNode(s1,t0,carbonara), 0)

        comparisonGraph.addEdge(ResultNode(s1,t0,place1), ResultNode(s0,t0,beer), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,place1), ResultNode(s0,t0,carbonara), 0)

        comparisonGraph.addEdge(ResultNode(s0,t0,veganPlace), ResultNode(s0,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,veganPlace), ResultNode(s1,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,veganPlace), ResultNode(s0,t0,bruschetta), 0)

        comparisonGraph.addEdge(ResultNode(s1,t0, veganPlace), ResultNode(s2,t1, veganPlace), 4)
        comparisonGraph.addEdge(ResultNode(s1,t0, bruschetta), ResultNode(s2,t1, bruschetta), 4)

        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s0,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s1,t0,bruschetta), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,r), ResultNode(s0,t0,bruschetta), 0)

        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s0,t0,carbonara), 0)
        comparisonGraph.addEdge(ResultNode(s0,t0,r), ResultNode(s1,t0,carbonara), 0)
        comparisonGraph.addEdge(ResultNode(s1,t0,r), ResultNode(s0,t0,carbonara), 0)

        return comparisonGraph;
    }

    fun generateTestTable(ec: ELHIExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ELHISPLoopTable {
        val spTable = ELHISPLoopTable();

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val beerRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(beer)

        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val place1 = ec.parser.getNamedIndividual("place1")!!;
        val place2 = ec.parser.getNamedIndividual("place2")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!
        val veganPlaceRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(veganPlace)

        val serves = ec.parser.getOWLObjectProperty("serves")!!
        val serves_drink = ec.parser.getOWLObjectProperty("serves_drink")!!
        val serves_meal = ec.parser.getOWLObjectProperty("serves_meal")!!

        val e1 = IndividualLoopTableEntry(Pair(s0,t0), Pair(s1,t0), beerRes);
        val e1Val = 4;

        val e2 = IndividualLoopTableEntry(Pair(s0,t1), Pair(s1,t1), beerRes);

        val e3 = IndividualLoopTableEntry(Pair(s0,t0), Pair(s1,t0), veganPlaceRes);

        spTable.set(e1, 4)
        spTable.set(e2, 7)
        spTable.set(e3, 13)

        return spTable;
    }
}