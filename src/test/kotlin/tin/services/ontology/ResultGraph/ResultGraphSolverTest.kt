package tin.services.ontology.ResultGraph

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.QueryReaderServiceV2
import tin.services.internal.fileReaders.TransducerReaderServiceV2
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.technical.SystemConfigurationService
import java.io.File


@SpringBootTest
@TestConfiguration
class ResultGraphSolverTest {
    val resultGraphTestUtils = ResultGraphTestUtils();

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
    fun testShortestPathCalculation() {
        val manager = loadExampleOntology("pizza_4.rdf");
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC);
        val query = readQueryWithFileReaderService("resultGraph/test1.txt")
        val transducer = readTransducerWithFileReaderService("resultGraph/test1.txt")

        val queryGraph = query.graph;
        val transducerGraph = transducer.graph;

        val testGraph = resultGraphTestUtils.buildComparisonGraph(ec, queryGraph, transducerGraph)
        val fwSolver = FloydWarshallSolver(testGraph)

        val allPathsList = fwSolver.getAllShortestPaths()

        val s0 = queryGraph.getNode("s0")!!
        val s2 = queryGraph.getNode("s2")!!

        val t0 = transducerGraph.getNode("t0")!!
        val t1 = transducerGraph.getNode("t1")!!

        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!

        val s0t0VeganPlace = ResultNode(s0, t0, veganPlace);
        val s2t1VeganPlace = ResultNode(s2, t1, veganPlace);
        val s2t1Bruschetta = ResultNode(s2, t1, bruschetta);

        val s0t0r = ResultNode(s0, t0, r)

        assert(allPathsList.size == 3 )
        assert(allPathsList.contains(ShortestPathResult(s0t0VeganPlace, s2t1Bruschetta, 4)))
        assert(allPathsList.contains(ShortestPathResult(s0t0r, s2t1Bruschetta, 4)))
        assert(allPathsList.contains(ShortestPathResult(s0t0VeganPlace, s2t1VeganPlace, 17)))

        assert(fwSolver.getDistance(s0t0VeganPlace, s2t1Bruschetta) == ShortestPathResult(s0t0VeganPlace, s2t1Bruschetta, 4))
        assert(fwSolver.getDistance(s0t0r, s2t1Bruschetta) == ShortestPathResult(s0t0r, s2t1Bruschetta, 4))
        assert(fwSolver.getDistance(s0t0VeganPlace, s2t1VeganPlace) == ShortestPathResult(s0t0VeganPlace, s2t1VeganPlace, 17))

        //test dijkstra
        val dijkstraSolver = DijkstraSolver(testGraph);
        assert(dijkstraSolver.getDistance(s0t0VeganPlace, s2t1Bruschetta) == ShortestPathResult(s0t0VeganPlace, s2t1Bruschetta, 4))
        assert(dijkstraSolver.getDistance(s0t0r, s2t1Bruschetta) == ShortestPathResult(s0t0r, s2t1Bruschetta, 4))
        assert(dijkstraSolver.getDistance(s0t0VeganPlace, s2t1VeganPlace) == ShortestPathResult(s0t0VeganPlace, s2t1VeganPlace, 17))

    }

}