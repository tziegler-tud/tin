package tinCORE.services.ontology.ResultGraph

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import tinDL.model.v2.ResultGraph.DlResultGraph
import tinDL.model.v2.ResultGraph.DlResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinCORE.services.internal.fileReaders.*
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService
import tinDL.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.ResultGraph.ELResultGraphBuilder
import tinDL.services.ontology.loopTable.LoopTable.ELH.ELSPLoopTable
import tinDL.services.ontology.loopTable.loopTableEntry.ELH.ELSPLoopTableEntry
import java.io.File

@SpringBootTest
@TestConfiguration
class ELResultGraphBuilderTest {
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
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH);
        val query = readQueryWithFileReaderService("resultGraph/test1.txt")
        val transducer = readTransducerWithFileReaderService("resultGraph/test1.txt")

        val resultGraphBuilder = ELResultGraphBuilder(ec, query.graph, transducer.graph)
        val restrictedGraph = resultGraphBuilder.constructRestrictedGraph();
        val comparisonGraph = buildComparisonGraphRestricted(ec, query.graph, transducer.graph);

        assert(restrictedGraph == comparisonGraph)
    }

    @Test
    fun testResultGraphConstruction() {
        val manager = loadExampleOntology("pizza_4.rdf");
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH);
        val query = readQueryWithFileReaderService("resultGraph/test1.txt")
        val transducer = readTransducerWithFileReaderService("resultGraph/test1.txt")


        val testTable = generateTestTable(ec, query.graph, transducer.graph);
        val resultGraphBuilder = ELResultGraphBuilder(ec, query.graph, transducer.graph)
        val restrictedGraph = resultGraphBuilder.constructRestrictedGraph();
        val resultGraph = resultGraphBuilder.constructResultGraph(testTable);
        val comparisonGraph = buildComparisonGraph(ec, query.graph, transducer.graph);

        assert(resultGraph == comparisonGraph)
    }

    fun buildComparisonGraph(ec: ELExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : DlResultGraph {
        val comparisonGraph = buildComparisonGraphRestricted(ec, queryGraph, transducerGraph);

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beer = ec.parser.getNamedIndividual("beer")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!

        comparisonGraph.addEdge(DlResultNode(s0,t0,beer), DlResultNode(s1,t0,beer), 4)
        comparisonGraph.addEdge(DlResultNode(s0,t1,beer), DlResultNode(s1,t1,beer), 7)
        comparisonGraph.addEdge(DlResultNode(s0,t0,veganPlace), DlResultNode(s1,t0,veganPlace), 13)

        return comparisonGraph;

    }

    fun buildComparisonGraphRestricted(ec: ELExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : DlResultGraph {
        //build comparison graph
        val comparisonGraph = DlResultGraph()

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

        queryGraph.nodes.forEach { queryNode ->
            transducerGraph.nodes.forEach { transducerNode ->

                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,beer, ))
                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,bruschetta))
                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,carbonara))
                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,place1))
                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,place2))
                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,r))
                comparisonGraph.addNode(DlResultNode(queryNode,transducerNode,veganPlace))
            }
        }

        comparisonGraph.addEdge(DlResultNode(s0,t0,place1), DlResultNode(s0,t0,beer), 0)
        comparisonGraph.addEdge(DlResultNode(s0,t0,place1), DlResultNode(s1,t0,beer), 0)
        comparisonGraph.addEdge(DlResultNode(s0,t0,place1), DlResultNode(s0,t0,carbonara), 0)
        comparisonGraph.addEdge(DlResultNode(s0,t0,place1), DlResultNode(s1,t0,carbonara), 0)

        comparisonGraph.addEdge(DlResultNode(s1,t0,place1), DlResultNode(s0,t0,beer), 0)
        comparisonGraph.addEdge(DlResultNode(s1,t0,place1), DlResultNode(s0,t0,carbonara), 0)

        comparisonGraph.addEdge(DlResultNode(s0,t0,veganPlace), DlResultNode(s0,t0,bruschetta), 0)
        comparisonGraph.addEdge(DlResultNode(s0,t0,veganPlace), DlResultNode(s1,t0,bruschetta), 0)
        comparisonGraph.addEdge(DlResultNode(s1,t0,veganPlace), DlResultNode(s0,t0,bruschetta), 0)

        comparisonGraph.addEdge(DlResultNode(s1,t0, veganPlace), DlResultNode(s2,t1, veganPlace), 4)
        comparisonGraph.addEdge(DlResultNode(s1,t0, bruschetta), DlResultNode(s2,t1, bruschetta), 4)

        comparisonGraph.addEdge(DlResultNode(s0,t0,r), DlResultNode(s0,t0,bruschetta), 0)
        comparisonGraph.addEdge(DlResultNode(s0,t0,r), DlResultNode(s1,t0,bruschetta), 0)
        comparisonGraph.addEdge(DlResultNode(s1,t0,r), DlResultNode(s0,t0,bruschetta), 0)

        comparisonGraph.addEdge(DlResultNode(s0,t0,r), DlResultNode(s0,t0,carbonara), 0)
        comparisonGraph.addEdge(DlResultNode(s0,t0,r), DlResultNode(s1,t0,carbonara), 0)
        comparisonGraph.addEdge(DlResultNode(s1,t0,r), DlResultNode(s0,t0,carbonara), 0)

        return comparisonGraph;
    }

    fun generateTestTable(ec: ELExecutionContext, queryGraph: QueryGraph, transducerGraph: TransducerGraph) : ELSPLoopTable {
        val spTable = ELSPLoopTable();

        val s0 = queryGraph.getNode("s0")!!
        val s1 = queryGraph.getNode("s1")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val beerRes = ec.spRestrictionBuilder.createConceptNameRestriction("Beer")
        val veganPlaceRes = ec.spRestrictionBuilder.createConceptNameRestriction("VeganRestaurant")

        val e1 = ELSPLoopTableEntry(Pair(s0,t0), Pair(s1,t0), beerRes);
        val e2 = ELSPLoopTableEntry(Pair(s0,t1), Pair(s1,t1), beerRes);
        val e3 = ELSPLoopTableEntry(Pair(s0,t0), Pair(s1,t0), veganPlaceRes);

        spTable.set(e1, 4)
        spTable.set(e2, 7)
        spTable.set(e3, 13)

        return spTable;
    }
}