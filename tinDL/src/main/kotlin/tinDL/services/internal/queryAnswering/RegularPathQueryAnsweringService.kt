package tinDL.services.internal.queryAnswering

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tinDL.model.v1.alphabet.Alphabet
import tinDL.model.v1.utils.ProductAutomatonTuple
import tinDL.model.v1.dataProvider.RegularPathQueryDataProvider
import tinDL.model.v1.productAutomaton.ProductAutomatonGraph
import tinDL.model.v1.query.QueryGraph
import tinDL.model.v1.queryResult.RegularPathQueryResult
import tinDL.model.v1.queryResult.QueryResultRepository
import tinDL.model.v1.queryTask.QueryTask
import tinDL.model.v1.queryTask.QueryTaskRepository
import tinDL.model.v1.queryTask.ComputationProperties
import tinDL.model.v1.queryResult.computationStatistics.ComputationStatistics
import tinDL.model.v1.queryResult.QueryResultStatus
import tinDL.model.v1.queryResult.computationStatistics.RegularPathComputationStatistics
import tinDL.model.v1.tintheweb.FileRepository
import tinDL.model.v1.transducer.TransducerGraph
import tinDL.services.internal.ProductAutomatonService
import tinDL.services.internal.dijkstra.DijkstraQueryAnsweringUtils
import tinDL.services.internal.dijkstra.algorithms.Dijkstra
import tinDL.services.internal.dijkstra.algorithms.DijkstraThreshold
import tinDL.services.internal.dijkstra.algorithms.DijkstraTopK
import tinDL.services.internal.utils.TransducerFactory
import tinDL.services.technical.SystemConfigurationService
import tinDL.utils.findByIdentifier
import tinDL.services.internal.fileReaders.DatabaseReaderService
import tinDL.services.internal.fileReaders.QueryReaderService
import tinDL.services.internal.fileReaders.TransducerReaderService
import tinDL.services.internal.fileReaders.fileReaderResult.FileReaderResult
import kotlin.system.measureNanoTime

@Service
class RegularPathQueryAnsweringService(
    private val fileRepository: FileRepository,
    private val queryTaskRepository: QueryTaskRepository,
    private val queryResultRepository: QueryResultRepository,

    ) {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    @Autowired
    lateinit var queryReaderService: QueryReaderService

    @Autowired
    lateinit var databaseReaderService: DatabaseReaderService

    @Autowired
    lateinit var transducerReaderService: TransducerReaderService

    @Autowired
    private lateinit var dijkstraQueryAnsweringUtils: DijkstraQueryAnsweringUtils

    @Transactional
    fun calculateQueryTask(queryTask: QueryTask): RegularPathQueryResult {
        // set status to calculating
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Calculating }
        queryTaskRepository.save(queryTask)

        val dataProvider = buildDataProvider(queryTask)
        var pairContainingCompStatsAndAnswerSet: Pair<ComputationStatistics, Set<RegularPathQueryResult.AnswerTriplet>>? =
            null

        var regularPathQueryResultStatus: QueryResultStatus = QueryResultStatus.NoError


        when (queryTask.computationProperties.computationModeEnum) {
            ComputationProperties.ComputationModeEnum.Dijkstra -> pairContainingCompStatsAndAnswerSet =
                calculateDijkstra(dataProvider)

            ComputationProperties.ComputationModeEnum.Threshold -> if (queryTask.computationProperties.thresholdValue == null) {
                regularPathQueryResultStatus = QueryResultStatus.ErrorInComputationMode
            } else {
                pairContainingCompStatsAndAnswerSet = calculateThreshold(
                    dataProvider, queryTask.computationProperties.thresholdValue
                )
            }

            ComputationProperties.ComputationModeEnum.TopK -> if (queryTask.computationProperties.topKValue == null) {
                regularPathQueryResultStatus = QueryResultStatus.ErrorInComputationMode
            } else {
                pairContainingCompStatsAndAnswerSet =
                    calculateTopK(dataProvider, queryTask.computationProperties.topKValue)
            }
        }

        val regularPathQueryResult =

            // return if an error was found
            if (regularPathQueryResultStatus != QueryResultStatus.NoError) {
                RegularPathQueryResult(
                    queryTask,
                    null,
                    regularPathQueryResultStatus,
                    null,
                    HashSet()
                )
            } else {
                // no error found
                RegularPathQueryResult(
                    queryTask,
                    pairContainingCompStatsAndAnswerSet!!.first,
                    QueryResultStatus.NoError,
                    null,
                    pairContainingCompStatsAndAnswerSet.second
                )
            }

        // set status to finished
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Finished }
        queryTaskRepository.save(queryTask)

        // save the queryResult
        queryResultRepository.save(regularPathQueryResult)

        return regularPathQueryResult
    }

    private fun buildDataProvider(data: QueryTask): RegularPathQueryDataProvider {

        // find files
        val queryFileDb = fileRepository.findByIdentifier(data.queryFileIdentifier)
        val databaseFileDb = fileRepository.findByIdentifier(data.dataSourceFileIdentifier)

        val queryReaderResult: FileReaderResult<QueryGraph> =
            queryReaderService.read(systemConfigurationService.getQueryPath(), queryFileDb.filename)
        val queryGraph = queryReaderResult.get()

        val databaseReaderResult =
            databaseReaderService.read(systemConfigurationService.getDatabasePath(), databaseFileDb.filename)
        val databaseGraph = databaseReaderResult.get()

        val transducerGraph: TransducerGraph
        val alphabet = Alphabet(queryGraph.alphabet)
        alphabet.addAlphabet(databaseGraph.alphabet)

        if (data.computationProperties.generateTransducer && data.computationProperties.transducerGeneration != null) {
            // generate transducer
            transducerGraph = when (data.computationProperties.transducerGeneration) {
                ComputationProperties.TransducerGeneration.ClassicalAnswersPreserving -> TransducerFactory.generateClassicAnswersTransducer(
                        alphabet
                )

                ComputationProperties.TransducerGeneration.EditDistance -> TransducerFactory.generateEditDistanceTransducer(
                        queryGraph.alphabet, databaseGraph.alphabet
                )
            }
        } else {
            // transducer file is provided -> no generation needed
            val transducerFileDb = fileRepository.findByIdentifier(data.transducerFileIdentifier!!)
            val transducerReaderResult =
                transducerReaderService.read(systemConfigurationService.getTransducerPath(), transducerFileDb.filename)
            transducerGraph = transducerReaderResult.get()
        }

        return RegularPathQueryDataProvider(
            queryGraph = queryGraph,
            transducerGraph = transducerGraph,
            databaseGraph = databaseGraph,
            sourceVariableName = null,
            targetVariableName = null,
            alphabet = alphabet
        )
    }

    @Transactional
    fun calculateDijkstra(regularPathQueryDataProvider: RegularPathQueryDataProvider): Pair<ComputationStatistics, Set<RegularPathQueryResult.AnswerTriplet>> {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(regularPathQueryDataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstra = Dijkstra(productAutomatonGraph)
            answerMap = dijkstra.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(answerMap)
        }

        val combinedTime = preprocessingTime + mainProcessingTime + postProcessingTime

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
           RegularPathComputationStatistics(
                preprocessingTime / 1000000,
                mainProcessingTime / 1000000,
                postProcessingTime / 1000000,
                combinedTime / 1000000
            ), transformedAnswerSet
        )
    }

    @Transactional
    fun calculateThreshold(
        regularPathQueryDataProvider: RegularPathQueryDataProvider, threshold: Double
    ): Pair<ComputationStatistics, Set<RegularPathQueryResult.AnswerTriplet>> {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(regularPathQueryDataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstraThreshold = DijkstraThreshold(productAutomatonGraph, threshold)
            answerMap = dijkstraThreshold.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            answerMap = trimAnswerMapToThreshold(answerMap, threshold)
            transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(answerMap)
        }

        val combinedTime = preprocessingTime + mainProcessingTime + postProcessingTime

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            RegularPathComputationStatistics(
                preprocessingTime / 1000000,
                mainProcessingTime / 1000000,
                postProcessingTime / 1000000,
                combinedTime / 1000000
            ), transformedAnswerSet
        )
    }

    @Transactional
    fun calculateTopK(
        regularPathQueryDataProvider: RegularPathQueryDataProvider,
        kValue: Int
    ): Pair<ComputationStatistics, Set<RegularPathQueryResult.AnswerTriplet>> {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        var answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(regularPathQueryDataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstraTopK = DijkstraTopK(productAutomatonGraph, kValue)
            answerMap = dijkstraTopK.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerSet: Set<RegularPathQueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            answerMap = trimAnswerMapToTopK(answerMap, kValue)
            transformedAnswerSet = dijkstraQueryAnsweringUtils.makeAnswerMapReadable(answerMap)
        }

        val combinedTime = preprocessingTime + mainProcessingTime + postProcessingTime

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
        RegularPathComputationStatistics(
                preprocessingTime / 1000000,
                mainProcessingTime / 1000000,
                postProcessingTime / 1000000,
                combinedTime / 1000000
            ), transformedAnswerSet
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
}