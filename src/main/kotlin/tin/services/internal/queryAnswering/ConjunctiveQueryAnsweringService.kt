package tin.services.internal.queryAnswering

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.data.internal.ConjunctiveComputationStatisticsData
import tin.model.alphabet.Alphabet
import tin.model.dataProvider.ConjunctiveQueryDataProvider
import tin.model.dataProvider.RegularPathQueryDataProvider
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.queryResult.*
import tin.model.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tin.model.queryResult.computationStatistics.RegularPathComputationStatistics
import tin.model.queryResult.conjunctiveQueryResult.ConjunctiveQueryAnswerMapping
import tin.model.queryResult.conjunctiveQueryResult.ConjunctiveQueryAnswerMappingRepository
import tin.model.queryResult.conjunctiveQueryResult.ConjunctiveQueryResult
import tin.model.queryTask.ComputationProperties
import tin.model.queryTask.QueryTask
import tin.model.queryTask.QueryTaskRepository
import tin.model.tintheweb.FileRepository
import tin.model.transducer.TransducerGraph
import tin.model.utils.ProductAutomatonTuple
import tin.services.internal.ProductAutomatonService
import tin.services.internal.dijkstra.DijkstraQueryAnsweringUtils
import tin.services.internal.dijkstra.algorithms.Dijkstra
import tin.services.internal.fileReaders.ConjunctiveQueryReaderService
import tin.services.internal.fileReaders.DatabaseReaderService
import tin.services.internal.fileReaders.TransducerReaderService
import tin.services.internal.queryAnswering.conjunctiveUtils.QueryConjunctReassembler
import tin.services.internal.queryAnswering.conjunctiveUtils.VariableMappingContainer
import tin.services.technical.SystemConfigurationService
import tin.utils.findByIdentifier
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
                            dataProvider, queryTask.computationProperties.thresholdValue
                        )
                    }

                ComputationProperties.ComputationModeEnum.TopK -> conjunctiveQueryResult =
                    if (queryTask.computationProperties.topKValue == null) {
                        buildDummyPairForErrorCase(queryTask)
                    } else {
                        calculateTopK(dataProvider, queryTask.computationProperties.topKValue)
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
        val databaseFileDb = fileRepository.findByIdentifier(data.databaseFileIdentifier)

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
                            QueryResult.QueryResultStatus.NoError,
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
            queryResultStatus = QueryResult.QueryResultStatus.NoError,
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
        threshold: Double
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {
        return TODO()
    }

    private fun calculateTopK(
        dataProvider: ConjunctiveQueryDataProvider,
        topK: Int
    ): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData> {
        return TODO()
    }

    private fun buildDummyPairForErrorCase(queryTask: QueryTask): Pair<ConjunctiveQueryResult, ConjunctiveComputationStatisticsData?> {
        return Pair(
            ConjunctiveQueryResult(
                queryTask,
                null,
                QueryResult.QueryResultStatus.ErrorInComputationMode,
                emptySet(),
                emptySet()
            ), null
        )
    }
}