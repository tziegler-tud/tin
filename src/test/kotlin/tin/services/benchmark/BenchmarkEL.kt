package tin.services.benchmark

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.ResultGraph.ResultNode
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.Task.Benchmark.*
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.QueryReaderServiceV2
import tin.services.internal.fileReaders.TransducerReaderServiceV2
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.internal.utils.DLTransducerFactory
import tin.services.internal.utils.RandomQueryFactory
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.ResultGraph.ELHIResultGraphBuilder
import tin.services.ontology.ResultGraph.ELResultGraphBuilder
import tin.services.ontology.ResultGraph.FloydWarshallSolver
import tin.services.ontology.ResultGraph.ShortestPathResult
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPLoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPLoopTableBuilder
import tin.services.ontology.loopTable.loopTableEntry.ELHI.ELHISPALoopTableEntry
import tin.services.ontology.loopTable.loopTableEntry.IndividualLoopTableEntry
import tin.services.technical.SystemConfigurationService
import java.io.File
import kotlin.time.Duration
import kotlin.time.TimeSource


@SpringBootTest
@TestConfiguration
class BenchmarkEL {
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
    fun testGetTopClassNode() {

        //load ontology
        val manager = loadExampleOntology("SNOMED/snomed-2024-09.owl")
        println("finishes loading ontology.")
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH, false);
        val reasoner = ec.dlReasoner;
        val topNode = ec.dlReasoner.getTopClassNode();
        println("found!")
    }

    @Test
    fun testQueryAnswering() {

        //load ontology
        val timeSource = TimeSource.Monotonic


        val ontoStart = timeSource.markNow()
        val ontoName = "pizza3.rdf"
        val manager = loadExampleOntology(ontoName)
        val ontoEnd = timeSource.markNow()

        val initStart = timeSource.markNow()
        val ec = manager.createELExecutionContext(ExecutionContextType.ELH, false);
        val initEnd = timeSource.markNow();
        val queryAmount = 1000;

        val results = mutableListOf<TaskProcessingBenchmarkResult>();
        val startAllQueries = timeSource.markNow()

        val queryStates = 5
        val queryEdges = 10

        for (i in 0 until queryAmount) {
            println("Calculating query $i / $queryAmount")

            val queryGraph = RandomQueryFactory.generateRandomQuery(queryStates,queryEdges, 15);
            val transducerGraph = DLTransducerFactory.generateEditDistanceTransducer(queryGraph, ec);
            val queryInitialTime = timeSource.markNow()
//        ec.prewarmSubsumptionCache()
            val builder = ELSPALoopTableBuilder(queryGraph, transducerGraph, manager, ec);
            val spBuilder = ELSPLoopTableBuilder(queryGraph, transducerGraph, manager, ec);

            val startTime = timeSource.markNow()

//        builder.calculateWithDepthLimit(iterationLimit);
            println("Calculating spa table...")

            val spaTable = builder.calculateFullTable();

            val spaEndTime = timeSource.markNow()
            println("Calculating sp table...")
            val spTable = spBuilder.calculateFullTable(spaTable);
            val spEndTime = timeSource.markNow()

            val resultGraphBuilder = ELResultGraphBuilder(ec, queryGraph ,transducerGraph)
            val resultGraphStartTime = timeSource.markNow()
            val resultGraph = resultGraphBuilder.constructResultGraph(spTable);
            val resultGraphEndTime = timeSource.markNow();

            val prewarmTime = startTime - queryInitialTime;
            val spaTime = spaEndTime - startTime;
            val spTime = spEndTime - spaEndTime;
            val resultGraphTime = resultGraphEndTime - resultGraphStartTime;
            val solverStartTime = timeSource.markNow();
            val solver = FloydWarshallSolver(resultGraph);
            val solverEndTime = timeSource.markNow();
            val resultList = solver.getAllShortestPaths()
//            val resultMap = solver.getShortestPathMap();
            val totalTime = resultGraphEndTime - queryInitialTime;


            println("Total computation time: " + totalTime)
            println("Cache prewarming: " + prewarmTime)
            println("SPA computation time: " + spaTime)
            println("SP computation time: " + spTime)
            println("ResultGraph computation time: " + resultGraphTime)

            val stats = builder.getExecutionContext().dlReasoner.getStats();



            val times = TaskProcessingResultTimes(startTime, spaEndTime, spaEndTime, spEndTime, resultGraphStartTime, resultGraphEndTime, solverStartTime, solverEndTime)
            val reasonerStats = TaskProcessingReasonerStats(stats)
            val spa = TaskProcessingSpaBuilderStats(builder.statsTotalIterations, builder.statsTotalSize, builder.statsMaxPossibleSize)
            val sp = TaskProcessingSpBuilderStats(spBuilder.statsTotalSize, spBuilder.statsMaxPossibleSize)
            val resultStats = TaskProcessingResultBuilderStats(resultGraph.nodes.size, resultGraph.edges.size, resultGraphBuilder.maxEdgeCost, resultGraphBuilder.minEdgeCost, resultGraphBuilder.unreachableNodesAmount)
            val benchmarkResult = TaskProcessingBenchmarkResult(times, reasonerStats, spa, sp, resultStats)
            results.add(benchmarkResult);
        }

        val endAllQueries = timeSource.markNow()


        var spaSum: Duration = kotlin.time.Duration.ZERO
        var spSum: Duration = kotlin.time.Duration.ZERO
        var resultSum: Duration = kotlin.time.Duration.ZERO

        var totalIterationsSum = 0;
        var totalSizeSum = 0;

        var maxIterations = 0;
        var minIterations = Int.MAX_VALUE;


        var maxSize = 0;
        var minSize = Int.MAX_VALUE;
        var avgSize = 0

        var maxSpa: Duration = kotlin.time.Duration.ZERO
        var minSpa: Duration = kotlin.time.Duration.INFINITE
        var maxSp: Duration = kotlin.time.Duration.ZERO
        var minSp: Duration = kotlin.time.Duration.INFINITE

        results.forEach { benchmarkResult ->
            spaSum += benchmarkResult.times.spaTime
            spSum += benchmarkResult.times.spTime
            resultSum += benchmarkResult.times.resultGraphTime

            totalIterationsSum += benchmarkResult.spaBuilderStats.totalIterations
            totalSizeSum += benchmarkResult.spaBuilderStats.tableSize;

            if(benchmarkResult.times.spaTime < minSpa) minSpa = benchmarkResult.times.spaTime
            if(benchmarkResult.times.spTime < minSp) minSp = benchmarkResult.times.spTime

            if(benchmarkResult.times.spaTime > maxSpa) maxSpa = benchmarkResult.times.spaTime
            if(benchmarkResult.times.spTime > maxSp) maxSp = benchmarkResult.times.spTime

            if(benchmarkResult.spaBuilderStats.totalIterations < minIterations) minIterations = benchmarkResult.spaBuilderStats.totalIterations
            if(benchmarkResult.spaBuilderStats.totalIterations > maxIterations) maxIterations = benchmarkResult.spaBuilderStats.totalIterations

            if(benchmarkResult.spaBuilderStats.tableSize < minSize) minSize = benchmarkResult.spaBuilderStats.tableSize
            if(benchmarkResult.spaBuilderStats.tableSize > maxSize) maxSize = benchmarkResult.spaBuilderStats.tableSize



        }

        val avgTime = (endAllQueries - startAllQueries) / queryAmount
        val avgSpa = spaSum / queryAmount
        val avgSp = spSum / queryAmount
        val avgResult = resultSum / queryAmount

        val avgIterations = totalIterationsSum / queryAmount
        val avgSpaSize = totalSizeSum / queryAmount

        //results
        println("___________________________________________________________________________________________________________")
        println("                                       RESULTS")
        println(" ontology:     $ontoName")
        println(" mode:         ELH")
        println(" query amount: $queryAmount")
        println(" query size: + $queryStates / $queryEdges")
        println(" trans:        editDistance")
        println("___________________________________________________________________________________________________________")

        println("Loading Ontology: " + (ontoEnd - ontoStart))
        println("Average query time: " + avgTime + "\n")
        println("-------------------------------------------------")
        println("Average SPA Time: " + avgSpa)
        println("Minimal spa time: " + minSpa)
        println("Maximal spa time: " + maxSpa)
        println("----------------")
        println("Average spa iterations: " + avgIterations)
        println("Minimal spa iterations: " + minIterations)
        println("Maximal spa iterations: " + maxIterations)
        println("----------------")
        println("Average spa size: " + avgSpaSize)
        println("Minimal spa size: " + minSize)
        println("Maximal spa size: " + maxSize)
        println("-------------------------------------------------")
        println("Average SP Time: " + avgSp)
        println("Minimal sp time: " + minSp)
        println("Maximal sp time: " + maxSp)
    }
}