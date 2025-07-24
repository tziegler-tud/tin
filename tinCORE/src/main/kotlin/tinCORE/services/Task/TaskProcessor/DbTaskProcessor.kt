package tinCORE.services.Task.TaskProcessor

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinCORE.data.Task.*
import tinCORE.data.Task.DbTask.Benchmark.DbTaskProcessingBenchmarkResult
import tinCORE.data.Task.DbTask.DbComputationMode
import tinCORE.data.Task.DbTask.DbTask
import tinCORE.data.Task.DbTask.DbTaskComputationConfiguration

import tinCORE.data.tinDB.queryResult.RegularPathQueryResult
import tinCORE.data.tinDB.queryResult.computationStatistics.RegularPathComputationStatistics
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v2.productAutomaton.ProductAutomatonGraph
import tinDB.model.v2.utils.ProductAutomatonTuple
import tinDB.model.v2.ResultGraph.DbResultNode
import tinDB.services.internal.ProductAutomatonServiceV2
import tinDB.services.internal.dijkstra.algorithms.Dijkstra
import tinDB.services.internal.dijkstra.algorithms.DijkstraThreshold
import tinDB.services.internal.dijkstra.algorithms.DijkstraTopK

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.services.ResultGraph.ShortestPathResult
import kotlin.system.measureNanoTime
import kotlin.time.Duration

import kotlin.time.TimeSource

class DbTaskProcessor(
    override val task: DbTask,
    override val queryGraph: QueryGraph,
    override val transducerMode: TransducerMode,
    override val transducerGenerationMode: TransducerGenerationMode? = null,
    override val transducerGraphProvided: TransducerGraph? = null,
    val databaseGraph: DatabaseGraph
) : AbstractTaskProcessor<DbResultNode>(
    task,
    queryGraph,
    transducerMode,
    transducerGenerationMode,
    transducerGraphProvided,
) {

    private val compConfig: DbTaskComputationConfiguration = task.getComputationConfiguration();
    private val fileConfig = task.getFileConfiguration();
    private var preprocessingTime: Long = 0


    private var benchmarkResults: TaskProcessingBenchmarkResult? = null;

    constructor(
        task: DbTask,
        queryGraph: QueryGraph,
        transducerGraph: TransducerGraph,
        databaseGraph: DatabaseGraph,
    ): this(task, queryGraph, TransducerMode.provided, null, transducerGraph, databaseGraph)

    override fun execute(): TaskProcessorExecutionResult<DbResultNode> {
        //execute task

        var results: MutableList<ShortestPathResult<DbResultNode>> = mutableListOf();

        val transducerGraph = buildTransducerGraph(transducerMode, transducerGenerationMode, queryGraph.generateAlphabet(), databaseGraph.generateAlphabet())

        var calculationResult: CalculationResult

        val dataProvider: RegularPathQueryDataProvider = RegularPathQueryDataProvider(
            queryGraph,
            transducerGraph,
            databaseGraph,
            null,
            null
        )

        val productAutomatonService = ProductAutomatonServiceV2(dataProvider)
        val productAutomatonGraph: ProductAutomatonGraph

        preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton()
        }

        when (compConfig.computationMode) {

            DbComputationMode.Dijkstra -> calculationResult =
                calculateDijkstra(productAutomatonGraph)

            DbComputationMode.Threshold -> if (compConfig.maxCost == null) {
                throw IllegalArgumentException("Failed to process task: Threshold mode selected, but threshold value not given.")
            } else {
                calculationResult = calculateThreshold(
                    productAutomatonGraph, compConfig.maxCost
                )
            }

            DbComputationMode.TopK -> if (compConfig.topKValue == null) {
                throw IllegalArgumentException("Failed to process task: TopK mode selected, but topK amount not given.")
            } else {
                calculationResult =
                    calculateTopK(productAutomatonGraph, compConfig.topKValue)
            }
            else -> {
                // unable to process. Invalid argument
                throw IllegalArgumentException("Failed to process task: ComputationMode not set.")

            }
        }

//        val regularPathQueryResult = RegularPathQueryResult (
//            task,
//            calculationResult.regularPathComputationStatistics
//            QueryResultStatus.NoError,
//            null,
//            calculationResult.transformedAnswerSet
//        )

        for ((productAutomatonTuple, cost) in calculationResult.answerMap) {
            val result = ShortestPathResult<DbResultNode>(
                source = DbResultNode(productAutomatonTuple.sourceProductAutomatonNode!!),
                target = DbResultNode(productAutomatonTuple.targetProductAutomatonNode),
                cost = cost.toInt()
            )
            results.add(result)
        }


        benchmarkResults = DbTaskProcessingBenchmarkResult()

        return TaskProcessorExecutionResult(
            results,
            benchmarkResults!!
        )
    }

    private fun calculateDijkstra(productAutomatonGraph: ProductAutomatonGraph): CalculationResult {

        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val mainProcessingTime = measureNanoTime {
            val dijkstra = Dijkstra(productAutomatonGraph)
            answerMap = dijkstra.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            transformedAnswerSet = makeAnswerMapReadable(answerMap)
        }

        val combinedTime = preprocessingTime + mainProcessingTime + postProcessingTime

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return CalculationResult(
            RegularPathComputationStatistics(
                preprocessingTime / 1000000,
                mainProcessingTime / 1000000,
                postProcessingTime / 1000000,
                combinedTime / 1000000
            ),
            answerMap,
            transformedAnswerSet
        )
    }

    private fun calculateThreshold(
        productAutomatonGraph: ProductAutomatonGraph, threshold: Int
    ): CalculationResult {

        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val mainProcessingTime = measureNanoTime {
            val dijkstraThreshold = DijkstraThreshold(productAutomatonGraph, threshold)
            answerMap = dijkstraThreshold.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            answerMap = trimAnswerMapToThreshold(answerMap, threshold.toDouble())
            transformedAnswerSet = makeAnswerMapReadable(answerMap)
        }

        val combinedTime = preprocessingTime + mainProcessingTime + postProcessingTime

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return CalculationResult(
            RegularPathComputationStatistics(
                preprocessingTime / 1000000,
                mainProcessingTime / 1000000,
                postProcessingTime / 1000000,
                combinedTime / 1000000
            ),
            answerMap,
            transformedAnswerSet
        )
    }

    private fun calculateTopK(
        productAutomatonGraph: ProductAutomatonGraph,
        kValue: Int
    ): CalculationResult {


        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val mainProcessingTime = measureNanoTime {
            val dijkstraTopK = DijkstraTopK(productAutomatonGraph, kValue)
            answerMap = dijkstraTopK.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            answerMap = trimAnswerMapToTopK(answerMap, kValue)
            transformedAnswerSet = makeAnswerMapReadable(answerMap)
        }

        val combinedTime = preprocessingTime + mainProcessingTime + postProcessingTime

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return CalculationResult(
            RegularPathComputationStatistics(
                preprocessingTime / 1000000,
                mainProcessingTime / 1000000,
                postProcessingTime / 1000000,
                combinedTime / 1000000
            ),
            answerMap,
            transformedAnswerSet
        )
    }

    private fun trimAnswerMapToTopK(
        answerMap: HashMap<ProductAutomatonTuple, Double>,
        topK: Int
    ): HashMap<ProductAutomatonTuple, Double> {
        // sort the answerMap ascending by the Double value, then take the first topK elements, and return them as a new HashMap
        return HashMap(answerMap.toList().sortedBy { (_, value) -> value }.take(topK).toMap())
    }

    private fun trimAnswerMapToThreshold(
        answerMap: HashMap<ProductAutomatonTuple, Double>,
        threshold: Double
    ): HashMap<ProductAutomatonTuple, Double> {
        // remove all elements whose value is larger than the threshold, then return the remaining HashMap
        return HashMap(answerMap.filter { (_, value) -> value <= threshold })
    }


    /**
     * transforms internal answerMap containing a (source, target) ProductAutomatonTuple as key, and a Double as value (cost of reaching target from source)
     * into a set of AnswerTriplets (source.name, target.name, double); omitting the technical ProductAutomatonNodes
     * after finishing the query we do not care about technical details, we simply want the (human-readable) results.
     */
    private fun makeAnswerMapReadable(
        answerMap: HashMap<ProductAutomatonTuple, Double>
    ): Set<RegularPathQueryResult.AnswerTriplet> {
        return HashSet<RegularPathQueryResult.AnswerTriplet>().apply {
            answerMap.forEach { (key, value) ->
                val source = key.sourceProductAutomatonNode!!.databaseNode.identifier
                val target = key.targetProductAutomatonNode.databaseNode.identifier
                add(RegularPathQueryResult.AnswerTriplet(source, target, value))
            }
        }
    }


}

class CalculationResult(
    val regularPathComputationStatistics : RegularPathComputationStatistics,
    val answerMap: HashMap<ProductAutomatonTuple, Double>,
    val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
)