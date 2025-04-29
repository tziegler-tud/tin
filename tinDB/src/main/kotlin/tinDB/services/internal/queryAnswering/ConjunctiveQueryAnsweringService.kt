package tinDB.services.internal.queryAnswering

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tinDB.data.internal.ConjunctiveComputationStatisticsData
import tinDB.model.v1.dataProvider.ConjunctiveQueryDataProvider
import tinDB.model.v1.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v1.productAutomaton.ProductAutomatonGraph
import tinDB.model.v1.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tinDB.model.v1.queryResult.computationStatistics.RegularPathComputationStatistics
import tinDB.model.v1.queryResult.conjunctiveQueryResult.ConjunctiveQueryAnswerMapping
import tinDB.model.v1.queryResult.conjunctiveQueryResult.ConjunctiveQueryAnswerMappingRepository
import tinDB.model.v1.queryResult.conjunctiveQueryResult.ConjunctiveQueryResult
import tinDB.model.v1.queryTask.ComputationProperties
import tinDB.model.v1.queryTask.QueryTask
import tinDB.model.v1.queryTask.QueryTaskRepository
import tinDB.model.v1.tintheweb.FileRepository
import tinDB.model.v1.transducer.TransducerGraph
import tinDB.model.v1.utils.ProductAutomatonTuple
import tinDB.model.v1.queryResult.QueryResultRepository
import tinDB.model.v1.queryResult.QueryResultStatus
import tinDB.model.v1.queryResult.RegularPathQueryResult
import tinDB.services.internal.ProductAutomatonService
import tinDB.services.internal.dijkstra.DijkstraQueryAnsweringUtils
import tinDB.services.internal.dijkstra.algorithms.Dijkstra
import tinDB.services.internal.dijkstra.algorithms.DijkstraThreshold
import tinDB.services.internal.fileReaders.ConjunctiveQueryReaderService
import tinDB.services.internal.fileReaders.DatabaseReaderService
import tinDB.services.internal.fileReaders.TransducerReaderService
import tinDB.services.internal.queryAnswering.conjunctiveUtils.QueryConjunctReassembler
import tinDB.services.internal.queryAnswering.conjunctiveUtils.VariableMappingContainer
import tinDB.services.technical.SystemConfigurationService
import tinDB.utils.findByIdentifier
import tinLIB.model.v2.alphabet.Alphabet
import kotlin.system.measureTimeMillis

@Service
class ConjunctiveQueryAnsweringService(
    private val fileRepository: FileRepository,
    private val queryTaskRepository: QueryTaskRepository,
    private val queryResultRepository: QueryResultRepository,
    private val conjunctiveQueryResultRepository: QueryResultRepository,
    private val conjunctiveQueryAnswerMappingRepository: ConjunctiveQueryAnswerMappingRepository,
) {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    @Autowired
    lateinit var conjunctiveQueryReaderService: ConjunctiveQueryReaderService

    @Autowired
    lateinit var databaseReaderService: DatabaseReaderService

    @Autowired
    lateinit var transducerReaderService: TransducerReaderService

    @Autowired
    lateinit var dijkstraQueryAnsweringUtils: DijkstraQueryAnsweringUtils

    @Autowired
    lateinit var queryConjunctReassembler: QueryConjunctReassembler

    @Transactional
    fun calculateQueryTask(queryTask: QueryTask): ConjunctiveQueryResult {
        // set status to calculating
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Calculating }
        queryTaskRepository.save(queryTask)

        val dataProvider: ConjunctiveQueryDataProvider

        val preProcessing = measureTimeMillis {
            dataProvider = buildDataProvider(queryTask)
        }

        val conjunctiveQueryResult: Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData?>

        /**
         * calculate result based on computation mode
         * looping over all query graphs is done within the helper functions
         */

        val mainProcessing = measureTimeMillis {

            when (queryTask.computationProperties.computationModeEnum) {
                ComputationProperties.ComputationModeEnum.Dijkstra ->
                    conjunctiveQueryResult =
                        calculateDijkstra(dataProvider, queryTask)


                ComputationProperties.ComputationModeEnum.Threshold -> conjunctiveQueryResult =
                    if (queryTask.computationProperties.thresholdValue == null) {
                        buildDummyPairForErrorCase(queryTask)
                    } else {
                        calculateThreshold(
                            dataProvider, queryTask
                        )
                    }

                ComputationProperties.ComputationModeEnum.TopK -> conjunctiveQueryResult =
                    if (queryTask.computationProperties.topKValue == null) {
                        buildDummyPairForErrorCase(queryTask)
                    } else {
                        calculateTopK(dataProvider, queryTask)
                    }
            }
        }


        // set queryTaskStatus to finished and save
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Finished }
        queryTaskRepository.save(queryTask)

        // time from preprocessing and mainprocessing are within this function, the others are retrieved from the conjunctiveQueryResult object

        conjunctiveQueryResult.first.apply {
            computationStatistics = ConjunctiveComputationStatistics(
                preProcessingTimeInMs = preProcessing,
                mainProcessingTimeInMs = mainProcessing,
                postProcessingTimeInMs = conjunctiveQueryResult.second!!.postProcessingTimeInMs,
                combinedTimeInMs = preProcessing + mainProcessing + conjunctiveQueryResult.second!!.postProcessingTimeInMs,
                combinedRPQPreProcessingTimeInMs = conjunctiveQueryResult.second!!.combinedRPQPreProcessingTimeInMs,
                combinedRPQMainProcessingTimeInMs = conjunctiveQueryResult.second!!.combinedRPQMainProcessingTimeInMs,
                combinedRPQPostProcessingTimeInMs = conjunctiveQueryResult.second!!.combinedRPQPostProcessingTimeInMs,
                combinedRPQTimeInMs = conjunctiveQueryResult.second!!.combinedRPQPreProcessingTimeInMs
                        + conjunctiveQueryResult.second!!.combinedRPQMainProcessingTimeInMs
                        + conjunctiveQueryResult.second!!.combinedRPQPostProcessingTimeInMs,
                reassemblyTimeInMs = conjunctiveQueryResult.second!!.reassemblyTimeInMs
            )
            variableMappings = conjunctiveQueryResult.first.variableMappings
        }
        return conjunctiveQueryResultRepository.save(conjunctiveQueryResult.first)

    }

    /**
     * reads the txt files and builds the data provider
     */
    private fun buildDataProvider(data: QueryTask): ConjunctiveQueryDataProvider {

        val queryFileDb = fileRepository.findByIdentifier(data.queryFileIdentifier)
        val databaseFileDb = fileRepository.findByIdentifier(data.dataSourceFileIdentifier)

        val queryFileReaderResult = conjunctiveQueryReaderService.read(
            systemConfigurationService.getConjunctiveQueryPath(),
            queryFileDb.filename
        )
        val databaseGraph =
            databaseReaderService.read(systemConfigurationService.getDatabasePath(), databaseFileDb.filename).get()

        val alphabet = Alphabet()

        // add all query alphabets
        queryFileReaderResult.graphMap.getMap().forEach { (graphName, graph) ->
            alphabet.addAlphabet(graph.alphabet)
        }

        // add database alphabet
        alphabet.addAlphabet(databaseGraph.alphabet)

        val transducerGraph: TransducerGraph
        if (data.computationProperties.generateTransducer && data.computationProperties.transducerGeneration != null) {
            // generate transducer
            transducerGraph = when (data.computationProperties.transducerGeneration) {
                ComputationProperties.TransducerGeneration.ClassicalAnswersPreserving -> transducerReaderService.generateClassicAnswersTransducer(
                    alphabet
                )

                ComputationProperties.TransducerGeneration.EditDistance -> transducerReaderService.generateEditDistanceTransducer(
                    alphabet
                )
            }
        } else {
            // transducer file is provided -> no generation needed
            val transducerFileDb = fileRepository.findByIdentifier(data.transducerFileIdentifier!!)
            val transducerReaderResult =
                transducerReaderService.read(systemConfigurationService.getTransducerPath(), transducerFileDb.filename)
            transducerGraph = transducerReaderResult.get()
        }

        return ConjunctiveQueryDataProvider(
            queryFileReaderResult.graphMap,
            queryFileReaderResult.formula,
            transducerGraph,
            databaseGraph,
            alphabet
        )
    }

    private fun calculateDijkstra(
        dataProvider: ConjunctiveQueryDataProvider,
        queryTask: QueryTask
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {
        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

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

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = Dijkstra(productAutomatonGraph)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    queryResultRepository.save(
                        RegularPathQueryResult(
                            queryTask,
                            RegularPathComputationStatistics(
                                localPreprocessingTime,
                                localMainProcessingTime,
                                localPostProcessingTime,
                                combinedProcessingTimes
                            ),
                            QueryResultStatus.NoError,
                            it.key,
                            transformedAnswerSet
                        )
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassemble(dataProvider, queryTask).toSet()

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
            queryTask = queryTask,
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

        return Pair(
            res, ConjunctiveComputationStatisticsData(
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
        )
    }

    private fun calculateThreshold(
        dataProvider: ConjunctiveQueryDataProvider,
        queryTask: QueryTask,
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {

        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

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

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = DijkstraThreshold(productAutomatonGraph, queryTask.computationProperties.thresholdValue!!)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    queryResultRepository.save(
                        RegularPathQueryResult(
                            queryTask,
                            RegularPathComputationStatistics(
                                localPreprocessingTime,
                                localMainProcessingTime,
                                localPostProcessingTime,
                                combinedProcessingTimes
                            ),
                            QueryResultStatus.NoError,
                            it.key,
                            transformedAnswerSet
                        )
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassembleThreshold(dataProvider, queryTask).toSet()

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
            queryTask = queryTask,
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

        return Pair(
            res, ConjunctiveComputationStatisticsData(
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
        )
    }

    private fun calculateTopK(
        dataProvider: ConjunctiveQueryDataProvider,
        queryTask: QueryTask,
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {
        /**
         * since we want to find the global optimum we must not use local optimization, thus we have to use "findAll" when calculating 2RPQs and sort/filter at the end.
         */

        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

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

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureTimeMillis {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        queryGraph = it.value,
                        transducerGraph = dataProvider.transducerGraph,
                        databaseGraph = dataProvider.databaseGraph,
                        sourceVariableName = dataProvider.conjunctiveFormula.regularPathQuerySourceVariableAssignment[it.key]!!,
                        targetVariableName = dataProvider.conjunctiveFormula.regularPathQueryTargetVariableAssignment[it.key]!!,
                    )
                )
            }

            localMainProcessingTime = measureTimeMillis {
                val dijkstra = Dijkstra(productAutomatonGraph)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureTimeMillis {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes: Long =
                localPreprocessingTime + localMainProcessingTime + localPostProcessingTime


            // persist each result separately
            // FYI: in smoke tests this takes approximately 50ms in total
            localInternalPostProcessingTime = measureTimeMillis {
                regularPathQueryResultSet.add(
                    queryResultRepository.save(
                        RegularPathQueryResult(
                            queryTask,
                            RegularPathComputationStatistics(
                                localPreprocessingTime,
                                localMainProcessingTime,
                                localPostProcessingTime,
                                combinedProcessingTimes
                            ),
                            QueryResultStatus.NoError,
                            it.key,
                            transformedAnswerSet
                        )
                    )
                )
            }


            totalRPQPreprocessingTime += localPreprocessingTime
            totalRPQMainProcessingTime += localMainProcessingTime
            totalRPQPostProcessingTime += localPostProcessingTime
            totalRPQInternalPostProcessingTime += localInternalPostProcessingTime
        }

        val variableMappingContainerSet: Set<VariableMappingContainer>

        val reassemblingTime = measureTimeMillis {
            variableMappingContainerSet = queryConjunctReassembler.reassemble(dataProvider, queryTask).toSet()

        }

        val sortedVariableMappingsContainerSet: Set<VariableMappingContainer>

        val postProcessing = measureTimeMillis {
            sortedVariableMappingsContainerSet = variableMappingContainerSet.sortedBy { it.cost }.take(queryTask.computationProperties.topKValue!!).toSet()
        }

        /**
         * build the final ConjunctiveQueryResult object such that we can reference it in the ConjunctiveQueryAnswerMapping
         */
        val combinedRPQTimeInMs = totalRPQPreprocessingTime + totalRPQMainProcessingTime + totalRPQPostProcessingTime
        val conjunctiveQueryResult = ConjunctiveQueryResult(
            queryTask = queryTask,
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

        return Pair(
            res, ConjunctiveComputationStatisticsData(
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
        )
    }

    private fun buildDummyPairForErrorCase(queryTask: QueryTask): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData?> {
        return Pair(
            ConjunctiveQueryResult(
                queryTask,
                null,
                QueryResultStatus.ErrorInComputationMode,
                emptySet(),
                emptySet()
            ), null
        )
    }
}