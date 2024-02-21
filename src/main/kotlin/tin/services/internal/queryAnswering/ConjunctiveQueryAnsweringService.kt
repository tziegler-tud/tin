package tin.services.internal.queryAnswering

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.model.alphabet.Alphabet
import tin.model.dataProvider.ConjunctiveQueryDataProvider
import tin.model.dataProvider.RegularPathQueryDataProvider
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.queryResult.*
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
import tin.services.technical.SystemConfigurationService
import tin.utils.findByIdentifier
import kotlin.system.measureNanoTime
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

        val conjunctiveQueryResult: ConjunctiveQueryResult

        /**
         * calculate result based on computation mode
         * looping over all query graphs is done within the helper functions
         */
        when (queryTask.computationProperties.computationModeEnum) {
            ComputationProperties.ComputationModeEnum.Dijkstra -> conjunctiveQueryResult =
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

        val mainProcessing = conjunctiveQueryResult.computationStatistics?.mainProcessingTimeInMs ?: -1
        val postProcessing = conjunctiveQueryResult.computationStatistics?.postProcessingTimeInMs ?: -1

        // set queryTaskStatus to finished and save
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Finished }
        queryTaskRepository.save(queryTask)
// this was commented out because we had doubled entities in the conjunctiveQueryAnswerMappingRepository. Idk what this was supposed to do
/*        conjunctiveQueryResult.variableMappings.forEach {
            conjunctiveQueryAnswerMappingRepository.save(
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = conjunctiveQueryResult,
                    existentiallyQuantifiedVariablesMapping =  it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            )
        }*/

        conjunctiveQueryResult.apply {
            computationStatistics = ComputationStatistics(
                preProcessing,
                mainProcessing,
                postProcessing,
                preProcessing + mainProcessing + postProcessing
            )
            variableMappings = conjunctiveQueryResult.variableMappings
        }
        return conjunctiveQueryResultRepository.save(conjunctiveQueryResult)

        /*
                // save queryResult
                return queryResultRepository.save(
                    ConjunctiveQueryResult(
                        queryTask,
                        ComputationStatistics(
                            preProcessing,
                            mainProcessing,
                            postProcessing,
                            preProcessing + mainProcessing + postProcessing
                        ),
                        QueryResult.QueryResultStatus.NoError,
                        conjunctiveQueryResult.variableAssignments,
                        conjunctiveQueryResult.regularPathQueryResults
                    )
                )*/
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
    ): ConjunctiveQueryResult {
        val productAutomatonService = ProductAutomatonService()
        var productAutomatonGraph: ProductAutomatonGraph
        var workingAlphabet: Alphabet
        var localAnswerMap: HashMap<ProductAutomatonTuple, Double>

        val totalRPQPreprocessingTime = 0L
        val totalRPQMainProcessingTime = 0L
        val totalRPQPostProcessingTime = 0L

        var localPreprocessingTime: Long
        var localMainProcessingTime: Long
        var localPostProcessingTime: Long

        val combiningTime = 0L

        val regularPathQueryResultSet = HashSet<RegularPathQueryResult>()

        dataProvider.conjunctiveQueryGraphMap.getMap().forEach {

            workingAlphabet = it.value.alphabet
            workingAlphabet.addAlphabet(dataProvider.databaseGraph.alphabet)

            localPreprocessingTime = measureNanoTime {
                productAutomatonGraph = productAutomatonService.constructProductAutomaton(
                    RegularPathQueryDataProvider(
                        it.value,
                        dataProvider.transducerGraph,
                        dataProvider.databaseGraph,
                    )
                )
            }

            localMainProcessingTime = measureNanoTime {
                val dijkstra = Dijkstra(productAutomatonGraph)
                localAnswerMap = dijkstra.processDijkstraOverAllInitialNodes()
            }

            val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
            localPostProcessingTime = measureNanoTime {
                transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(localAnswerMap)
            }

            val combinedProcessingTimes = localPreprocessingTime + localMainProcessingTime + localPostProcessingTime
            // persist each result separately
            regularPathQueryResultSet.add(
                queryResultRepository.save(
                    RegularPathQueryResult(
                        queryTask,
                        ComputationStatistics(
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

            totalRPQPreprocessingTime.plus(localPreprocessingTime)
            totalRPQMainProcessingTime.plus(localMainProcessingTime)
            totalRPQPostProcessingTime.plus(localPostProcessingTime)
        }

        val variableAssignmentContainerSet = queryConjunctReassembler.reassemble(dataProvider, queryTask).toSet()

/*        val conjunctiveQueryAnswerMappingSet: HashSet<ConjunctiveQueryAnswerMapping> =
            variableAssignmentContainerSet.map {
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = null,
                    existentiallyQuantifiedVariablesMapping = it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            }.toHashSet()*/

        /**
         * build the final ConjunctiveQueryResult object such that we can reference it in the ConjunctiveQueryAnswerMapping
         */
        val conjunctiveQueryResult = ConjunctiveQueryResult(
            queryTask = queryTask,
            computationStatistics = ComputationStatistics(
                totalRPQPreprocessingTime,
                totalRPQMainProcessingTime,
                totalRPQPostProcessingTime,
                totalRPQPreprocessingTime + totalRPQMainProcessingTime + totalRPQPostProcessingTime
            ),
            queryResultStatus = QueryResult.QueryResultStatus.NoError,
            variableMappings = emptySet(),
            regularPathQueryResults = regularPathQueryResultSet
        )

        /**
         * use the previously built ConjunctiveQueryResult object to build the ConjunctiveQueryAnswerMapping objects and store them in the ConjunctiveQueryResult
         */
        return conjunctiveQueryResult.apply {
            variableMappings = variableAssignmentContainerSet.map {
                ConjunctiveQueryAnswerMapping(
                    cost = it.cost,
                    conjunctiveQueryResult = conjunctiveQueryResult,
                    existentiallyQuantifiedVariablesMapping = it.existentiallyQuantifiedVariablesMapping,
                    answerVariablesMapping = it.answerVariablesMapping
                )
            }.toHashSet()
        }
    }

    private fun calculateThreshold(
        dataProvider: ConjunctiveQueryDataProvider,
        threshold: Double
    ): ConjunctiveQueryResult {
        return TODO()
    }

    private fun calculateTopK(
        dataProvider: ConjunctiveQueryDataProvider,
        topK: Int
    ): ConjunctiveQueryResult {
        return TODO()
    }

    private fun buildDummyPairForErrorCase(queryTask: QueryTask): ConjunctiveQueryResult {
        return ConjunctiveQueryResult(
            queryTask,
            null,
            QueryResult.QueryResultStatus.ErrorInComputationMode,
            emptySet(),
            emptySet()
        )
    }
}