package tinCORE.services.Task.TaskProcessor

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinCORE.data.Task.*
import tinCORE.data.Task.DbTask.Benchmark.DbTaskProcessingBenchmarkResult
import tinCORE.data.Task.DbTask.DbComputationMode
import tinCORE.data.Task.DbTask.DbTask
import tinCORE.data.Task.DbTask.DbTaskComputationConfiguration
import tinCORE.data.tinDB.queryResult.QueryResultStatus

import tinCORE.data.tinDB.queryResult.RegularPathQueryResult
import tinCORE.data.tinDB.queryResult.computationStatistics.RegularPathComputationStatistics
import tinCORE.data.tinDB.queryResult.conjunctiveQueryResult.ConjunctiveQueryResult
import tinDB.data.internal.ConjunctiveComputationStatisticsData
import tinDB.model.v1.queryResult.conjunctiveQueryResult.ConjunctiveQueryAnswerMapping
import tinDB.model.v2.ConjunctiveFormula
import tinDB.model.v2.ConjunctiveQueryGraphMap
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v2.productAutomaton.ProductAutomatonGraph
import tinDB.model.v2.utils.ProductAutomatonTuple
import tinDB.model.v2.ResultGraph.DbResultNode
import tinDB.model.v2.dataProvider.ConjunctiveQueryDataProvider
import tinDB.services.internal.ProductAutomatonServiceV2
import tinDB.services.internal.dijkstra.algorithms.Dijkstra
import tinDB.services.internal.dijkstra.algorithms.DijkstraThreshold
import tinDB.services.internal.dijkstra.algorithms.DijkstraTopK
import tinDB.services.internal.queryAnswering.conjunctiveUtils.QueryConjunctReassemblerV2
import tinDB.services.internal.queryAnswering.conjunctiveUtils.VariableMappingContainer
import tinLIB.model.v2.alphabet.Alphabet

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.services.ResultGraph.ShortestPathResult
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

import kotlin.time.TimeSource

class ConjunctiveDbTaskProcessor(
    override val task: DbTask,
    override val queryGraph: QueryGraph,
    override val transducerMode: TransducerMode,
    override val transducerGenerationMode: TransducerGenerationMode? = null,
    override val transducerGraphProvided: TransducerGraph? = null,
    val databaseGraph: DatabaseGraph,
    val conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
    val conjunctiveFormula: ConjunctiveFormula,
) : AbstractTaskProcessor<DbResultNode>(
    task,
    queryGraph,
    transducerMode,
    transducerGenerationMode,
    transducerGraphProvided,
) {

    private val compConfig: DbTaskComputationConfiguration = task.getComputationConfiguration();
    private val fileConfig = task.getFileConfiguration();

    private var benchmarkResults: TaskProcessingBenchmarkResult? = null;

    var queryConjunctReassembler: QueryConjunctReassemblerV2 = QueryConjunctReassemblerV2()


    constructor(
        task: DbTask,
        transducerGraph: TransducerGraph,
        databaseGraph: DatabaseGraph,
        conjunctiveQueryGraphMap: ConjunctiveQueryGraphMap,
        conjunctiveFormula: ConjunctiveFormula,
    ): this(task, QueryGraph(), TransducerMode.provided, null, transducerGraph, databaseGraph, conjunctiveQueryGraphMap, conjunctiveFormula)

    override fun execute(): TaskProcessorExecutionResult<DbResultNode> {
        //execute task

        var results: MutableList<ShortestPathResult<DbResultNode>> = mutableListOf();

        val transducerGraph = buildTransducerGraph(transducerMode, transducerGenerationMode, queryGraph.generateAlphabet(), databaseGraph.generateAlphabet())

        var calculationResult: ConjunctiveCalculationResult

        val dataProvider: ConjunctiveQueryDataProvider = ConjunctiveQueryDataProvider(
            conjunctiveQueryGraphMap,
            conjunctiveFormula,
            transducerGraph,
            databaseGraph,
        )

        when (compConfig.computationMode) {

            DbComputationMode.Dijkstra -> calculationResult =
                calculateDijkstra(dataProvider)

            DbComputationMode.Threshold -> if (compConfig.maxCost == null) {
                throw IllegalArgumentException("Failed to process task: Threshold mode selected, but threshold value not given.")
            } else {
                calculationResult = calculateThreshold(
                    dataProvider, compConfig.maxCost
                )
            }

            DbComputationMode.TopK -> if (compConfig.topKValue == null) {
                throw IllegalArgumentException("Failed to process task: TopK mode selected, but topK amount not given.")
            } else {
                calculationResult =
                    calculateTopK(dataProvider, compConfig.topKValue)
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

    private fun calculateDijkstra(
        dataProvider: ConjunctiveQueryDataProvider,
    ) : ConjunctiveCalculationResult {

        var productAutomatonService: ProductAutomatonServiceV2
        var productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

        var singleResultsAnswerMap: MutableMap<String, Set<ShortestPathResult<DbResultNode>>> = mutableMapOf()

        var totalRPQPreprocessingTime = 0L
        var totalRPQMainProcessingTime = 0L
        var totalRPQPostProcessingTime = 0L
        var totalRPQInternalPostProcessingTime = 0L

        var localPreprocessingTime: Long
        var localMainProcessingTime: Long
        var localPostProcessingTime: Long
        var localInternalPostProcessingTime: Long

        val regularPathQueryResultSet = HashSet<RegularPathQueryResult>()

        dataProvider.conjunctiveQueryGraphMap.getMap().forEach {

            val graphIdentifier = it.key

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonService = ProductAutomatonServiceV2(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
                productAutomatonGraph = productAutomatonService.constructProductAutomaton()

            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = Dijkstra(productAutomatonGraph)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    RegularPathQueryResult(
                        task,
                        RegularPathComputationStatistics(
                            localPreprocessingTime,
                            localMainProcessingTime,
                            localPostProcessingTime,
                            combinedProcessingTimes
                        ),
                        QueryResultStatus.NoError,
                        graphIdentifier,
                        transformedAnswerSet
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime

            val results: MutableSet<ShortestPathResult<DbResultNode>> = mutableSetOf()
            for ((productAutomatonTuple, cost) in localAnswerMap) {
                val result = ShortestPathResult<DbResultNode>(
                    source = DbResultNode(productAutomatonTuple.sourceProductAutomatonNode!!),
                    target = DbResultNode(productAutomatonTuple.targetProductAutomatonNode),
                    cost = cost.toInt()
                )
                results.add(result)
            }

            singleResultsAnswerMap[graphIdentifier] = results
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassemble(dataProvider, singleResultsAnswerMap ).toSet()

        }

        val sortedVariableMappingsContainerSet: Set<VariableMappingContainer>

        val postProcessing = measureTimeMillis {
            sortedVariableMappingsContainerSet = variableMappingContainerSet.sortedBy { it.cost }.toSet()
        }

        /**
         * build the final ConjunctiveQueryResult object such that we can reference it in the ConjunctiveQueryAnswerMapping
         */
        val combinedRPQTimeInMs = totalRPQPreprocessingTime + totalRPQMainProcessingTime + totalRPQPostProcessingTime
        val conjunctiveQueryResult = ConjunctiveQueryResult(
            queryTask = task,
            computationStatistics = null,
            queryResultStatus = QueryResultStatus.NoError,
            variableMappings = emptySet(),
            regularPathQueryResults = regularPathQueryResultSet
        )

        /**
         * use the previously built ConjunctiveQueryResult object to build the ConjunctiveQueryAnswerMapping objects and store them in the ConjunctiveQueryResult
         */
        val res = conjunctiveQueryResult.apply {
            variableMappings = sortedVariableMappingsContainerSet.map {
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = conjunctiveQueryResult,
                    existentiallyQuantifiedVariablesMapping = it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            }.toHashSet()
        }

        val statistics = ConjunctiveComputationStatisticsData(
                preProcessingTimeInMs = 0,
                mainProcessingTimeInMs = 0,
                postProcessingTimeInMs = postProcessing,
                combinedTimeInMs = 0,
                combinedRPQPreProcessingTimeInMs = totalRPQPreprocessingTime,
                combinedRPQMainProcessingTimeInMs = totalRPQMainProcessingTime,
                combinedRPQPostProcessingTimeInMs = totalRPQPostProcessingTime,
                combinedRPQInternalPostProcessingTimeInMs = totalRPQInternalPostProcessingTime,
                combinedRPQTimeInMs = combinedRPQTimeInMs,
                reassemblyTimeInMs = reassemblingTime
        )

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return ConjunctiveCalculationResult(
            statistics,
            answerMap,
            transformedAnswerSet
        )
    }

    private fun calculateThreshold(
        regularPathQueryDataProvider: RegularPathQueryDataProvider, threshold: Int
    ): ConjunctiveCalculationResult {

        val productAutomatonService = ProductAutomatonServiceV2(regularPathQueryDataProvider)
        val productAutomatonGraph: ProductAutomatonGraph
        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton()
        }

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
        return ConjunctiveCalculationResult(
            ConjunctiveComputationStatisticsData(
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
        regularPathQueryDataProvider: RegularPathQueryDataProvider,
        kValue: Int
    ): ConjunctiveCalculationResult {

        val productAutomatonService = ProductAutomatonServiceV2(regularPathQueryDataProvider)
        val productAutomatonGraph: ProductAutomatonGraph
        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton()
        }

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
        return ConjunctiveCalculationResult(
            ConjunctiveComputationStatisticsData(
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

class ConjunctiveCalculationResult(
    val conjunctiveComputationStatistics : ConjunctiveComputationStatisticsData,
    val answerMap: HashMap<ProductAutomatonTuple, Double>,
    val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
)