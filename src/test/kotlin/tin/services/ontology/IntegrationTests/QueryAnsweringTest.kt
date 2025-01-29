package tin.services.ontology.IntegrationTests

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
import tin.services.ontology.ResultGraph.ELHIResultGraphBuilder
import tin.services.ontology.ResultGraph.FloydWarshallSolver
import tin.services.ontology.ResultGraph.ResultGraphSolver
import tin.services.ontology.ResultGraph.ShortestPathResult
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPLoopTableBuilder
import tin.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.TimeSource

@SpringBootTest
@TestConfiguration
class QueryAnsweringTest {
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
    fun testQueryAnswering() {
        val manager = loadExampleOntology("pizza_4_1.rdf")
        val query = readQueryWithFileReaderService("integration/test_comp1.txt")
        val transducer = readTransducerWithFileReaderService("integration/test_comp1.txt")

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        val spBuilder = ELHISPLoopTableBuilder(query.graph, transducer.graph, manager, ec);

        val startTime = timeSource.markNow()

        val spaTable = builder.calculateFullTable();
        val spaEndTime = timeSource.markNow()
        val spTable = spBuilder.calculateFullTable(spaTable);
        val spEndTime = timeSource.markNow()

        val resultGraphBuilder = ELHIResultGraphBuilder(ec, query.graph ,transducer.graph)
        val resultGraphStartTime = timeSource.markNow()
        val resultGraph = resultGraphBuilder.constructResultGraph(spTable);
        val resultGraphEndTime = timeSource.markNow();

        val prewarmTime = startTime - initialTime;
        val spaTime = spaEndTime - startTime;
        val spTime = spEndTime - spaEndTime;
        val resultGraphTime = resultGraphStartTime - resultGraphEndTime;
        val totalTime = resultGraphEndTime - initialTime;


        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("SPA computation time: " + spaTime)
        println("SP computation time: " + spTime)
        println("ResultGraph computation time: " + resultGraphTime)

        val solver = FloydWarshallSolver(resultGraph);
        val resultList = solver.getAllShortestPaths()
        val resultMap = solver.getShortestPathMap();

        val s0 = query.graph.getNode("s0")!!
        val s1 = query.graph.getNode("s1")!!
        val s2 = query.graph.getNode("s2")!!
        val t0 = transducer.graph.getNode("t0")!!

        val bruschetta = manager.getQueryParser().getNamedIndividual("bruschetta")!!
        val carbonara = manager.getQueryParser().getNamedIndividual("carbonara")!!

        val bruschettaRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(bruschetta)
        val carbonaraRes = ec.spRestrictionBuilder.createNamedIndividualRestriction(carbonara)

        assert(spTable.get(IndividualLoopTableEntry(Pair(s0,t0),Pair(s1,t0),bruschettaRes)) == 34)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s1,t0),Pair(s0,t0),bruschettaRes)) == 34)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s0,t0),Pair(s2,t0),bruschettaRes)) == 59)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s1,t0),Pair(s2,t0),bruschettaRes)) == 25)

        assert(spTable.get(IndividualLoopTableEntry(Pair(s0,t0),Pair(s1,t0),carbonaraRes)) == 34)
        assert(spTable.get(IndividualLoopTableEntry(Pair(s1,t0),Pair(s0,t0),carbonaraRes)) == 34)
    }

    @Test
    fun testQueryAnswering2() {
        val manager = loadExampleOntology("pizza_4_1.obo")

        val query = readQueryWithFileReaderService("integration/test_comp2.txt")
        val transducer = readTransducerWithFileReaderService("integration/test_comp2.txt")

        val timeSource = TimeSource.Monotonic
        val initialTime = timeSource.markNow()
        val ec = manager.createELHIExecutionContext(ExecutionContextType.ELHI_NUMERIC, false);
        val builder = ELHISPALoopTableBuilder(query.graph, transducer.graph, manager, ec);
        val spBuilder = ELHISPLoopTableBuilder(query.graph, transducer.graph, manager, ec);

        val startTime = timeSource.markNow()
        val spaTable = builder.calculateFullTable();
        val spaEndTime = timeSource.markNow()

        val spTable = spBuilder.calculateFullTable(spaTable);
        val spEndTime = timeSource.markNow()

        val resultGraphBuilder = ELHIResultGraphBuilder(ec, query.graph ,transducer.graph)
        val resultGraphStartTime = timeSource.markNow()
        val resultGraph = resultGraphBuilder.constructResultGraph(spTable);
        val resultGraphEndTime = timeSource.markNow();

        val prewarmTime = startTime - initialTime;
        val spaTime = spaEndTime - startTime;
        val spTime = spEndTime - spaEndTime;
        val resultGraphTime = resultGraphStartTime - resultGraphEndTime;
        val totalTime = resultGraphEndTime - initialTime;


        println("Total computation time: " + totalTime)
        println("Cache prewarming: " + prewarmTime)
        println("SPA computation time: " + spaTime)
        println("SP computation time: " + spTime)
        println("ResultGraph computation time: " + resultGraphTime)

        val stats = builder.getExecutionContext().dlReasoner.getStats();

        val solver = FloydWarshallSolver(resultGraph);
        val resultList = solver.getAllShortestPaths()

        val s0 = query.graph.getNode("s0")!!
        val s2 = query.graph.getNode("s2")!!

        val t0 = transducer.graph.getNode("t0")!!
        val t1 = transducer.graph.getNode("t1")!!

        val bruschetta = ec.parser.getNamedIndividual("bruschetta")!!;
        val carbonara = ec.parser.getNamedIndividual("carbonara")!!;
        val r = ec.parser.getNamedIndividual("r")!!;
        val veganPlace = ec.parser.getNamedIndividual("VeganPlace")!!

        val s0t0VeganPlace = ResultNode(s0, t0, veganPlace);
        val s2t1Bruschetta = ResultNode(s2, t1, bruschetta);

        val s0t0r = ResultNode(s0, t0, r)

        assert(resultList.size == 2 )

        assert(solver.getShortestPath(veganPlace, bruschetta) == ShortestPathResult(s0t0VeganPlace, s2t1Bruschetta, 24));
        assert(solver.getShortestPath(r, bruschetta) == ShortestPathResult(s0t0r, s2t1Bruschetta, 24));

        assert(solver.getShortestPath(veganPlace, carbonara) == null);
    }
}