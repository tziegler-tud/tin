package tin.services.internal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.model.utils.ProductAutomatonTuple
import tin.data.internal.AnswerSetData
import tin.data.internal.ComputationStatisticsData
import tin.model.tintheweb.DataProvider
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.technical.QueryResult
import tin.model.technical.QueryTask
import tin.model.technical.QueryTaskRepository
import tin.model.technical.internal.ComputationProperties
import tin.model.technical.internal.ComputationStatistics
import tin.model.tintheweb.FileRepository
import tin.model.transducer.TransducerGraph
import tin.model.utils.PairOfStrings
import tin.services.internal.algorithms.Dijkstra
import tin.services.internal.algorithms.DijkstraThreshold
import tin.services.internal.algorithms.DijkstraTopK
import tin.services.technical.SystemConfigurationService
import tin.utils.findByIdentifier
import tin.services.internal.fileReaders.DatabaseReader
import tin.services.internal.fileReaders.QueryReader
import tin.services.internal.fileReaders.TransducerReader
import kotlin.system.measureNanoTime

@Service
class DijkstraQueryAnsweringService(
    private val fileRepository: FileRepository,
    private val queryTaskRepository: QueryTaskRepository,
) {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    @Transactional
    fun calculateQueryTask(queryTask: QueryTask): QueryResult {
        // set status to calculating
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Calculating }
        queryTaskRepository.save(queryTask)

        val computationStatistics: ComputationStatistics
        val dataProvider = buildDataProvider(queryTask)
        var pairContainingCompStatsAndAnswerSet: Pair<ComputationStatisticsData, AnswerSetData>? = null

        var queryResultStatus: QueryResult.QueryResultStatus = QueryResult.QueryResultStatus.NoError


        when (queryTask.computationMode.computationModeEnum) {
            tin.model.technical.internal.ComputationMode.ComputationModeEnum.Dijkstra -> pairContainingCompStatsAndAnswerSet =
                calculateDijkstra(dataProvider)

            tin.model.technical.internal.ComputationMode.ComputationModeEnum.Threshold -> if (queryTask.computationMode.computationProperties.thresholdValue == null) {
                queryResultStatus = QueryResult.QueryResultStatus.ErrorInComputationMode
            } else {
                pairContainingCompStatsAndAnswerSet = calculateThreshold(
                    dataProvider, queryTask.computationMode.computationProperties.thresholdValue
                )
            }

            tin.model.technical.internal.ComputationMode.ComputationModeEnum.TopK -> if (queryTask.computationMode.computationProperties.topKValue == null) {
                queryResultStatus = QueryResult.QueryResultStatus.ErrorInComputationMode
            } else {
                pairContainingCompStatsAndAnswerSet =
                    calculateTopK(dataProvider, queryTask.computationMode.computationProperties.topKValue)
            }
        }

        // return if an error was found
        return if (queryResultStatus != QueryResult.QueryResultStatus.NoError) {
            QueryResult(
                queryTask,
                null,
                queryResultStatus,
                HashMap()
            )
        } else {
            // no error found
            QueryResult(
                queryTask,
                ComputationStatistics(
                    pairContainingCompStatsAndAnswerSet!!.first.preProcessingTimeInMs,
                    pairContainingCompStatsAndAnswerSet.first.mainProcessingTimeInMs,
                    pairContainingCompStatsAndAnswerSet.first.postProcessingTimeInMs,
                ),
                QueryResult.QueryResultStatus.NoError,
                pairContainingCompStatsAndAnswerSet.second.answerMap.mapKeys { (key, _) -> key.toString() }
                    .toMutableMap().let { HashMap(it) }
            )
        }
    }

    private fun buildDataProvider(data: QueryTask): DataProvider {

        // find files
        val queryFileDb = fileRepository.findByIdentifier(data.queryFileIdentifier)
        val databaseFileDb = fileRepository.findByIdentifier(data.databaseFileIdentifier)

        val queryReader = QueryReader()
        val transducerReader = TransducerReader()
        val databaseReader = DatabaseReader()

        val queryGraph =
            queryReader.readRegularPathQueryFile(systemConfigurationService.uploadPathForQueries + queryFileDb.filename)
        val databaseGraph =
            databaseReader.readDatabaseFile(systemConfigurationService.uploadPathForDatabases + databaseFileDb.filename)

        val transducerGraph: TransducerGraph
        val alphabet = queryGraph.alphabet.plus(databaseGraph.alphabet)


        transducerGraph =
            if (data.computationMode.computationProperties.generateTransducer && data.computationMode.computationProperties.transducerGeneration != null) {
                // generate transducer
                when (data.computationMode.computationProperties.transducerGeneration) {
                    ComputationProperties.TransducerGeneration.ClassicalAnswersPreserving -> transducerReader.generateClassicAnswersTransducer(
                        alphabet
                    )

                    ComputationProperties.TransducerGeneration.EditDistance -> transducerReader.generateEditDistanceTransducer(
                        alphabet
                    )
                }
            } else {
                // transducer file is provided -> no generation needed
                val transducerFileDb = fileRepository.findByIdentifier(data.transducerFileIdentifier!!)
                transducerReader.readTransducerFile(systemConfigurationService.uploadPathForTransducers + transducerFileDb.filename)

            }

        return DataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)
    }

    @Transactional
    fun calculateDijkstra(dataProvider: DataProvider): Pair<ComputationStatisticsData, AnswerSetData> {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstra = Dijkstra(productAutomatonGraph)
            answerMap = dijkstra.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerMap: HashMap<PairOfStrings, Double>
        val postProcessingTime = measureNanoTime {
            transformedAnswerMap = makeAnswerMapReadable(answerMap)
        }

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            ComputationStatisticsData(
                preprocessingTime / 1000000, mainProcessingTime / 1000000, postProcessingTime / 1000000
            ), AnswerSetData(transformedAnswerMap)
        )
    }

    @Transactional
    fun calculateThreshold(
        dataProvider: DataProvider, threshold: Double
    ): Pair<ComputationStatisticsData, AnswerSetData> {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstraThreshold = DijkstraThreshold(productAutomatonGraph, threshold)
            answerMap = dijkstraThreshold.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerMap: HashMap<PairOfStrings, Double>
        val postProcessingTime = measureNanoTime {
            transformedAnswerMap = makeAnswerMapReadable(answerMap)
        }

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            ComputationStatisticsData(
                preprocessingTime / 1000000, mainProcessingTime / 1000000, postProcessingTime / 1000000
            ), AnswerSetData(transformedAnswerMap)
        )
    }

    @Transactional
    fun calculateTopK(dataProvider: DataProvider, kValue: Int): Pair<ComputationStatisticsData, AnswerSetData> {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstraTopK = DijkstraTopK(productAutomatonGraph, kValue)
            answerMap = dijkstraTopK.processDijkstraOverAllInitialNodes()
        }

        val transformedAnswerMap: HashMap<PairOfStrings, Double>
        val postProcessingTime = measureNanoTime {
            transformedAnswerMap = makeAnswerMapReadable(answerMap)
        }

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            ComputationStatisticsData(
                preprocessingTime / 1000000, mainProcessingTime / 1000000, postProcessingTime / 1000000
            ), AnswerSetData(transformedAnswerMap)
        )
    }

    private fun makeAnswerMapReadable(
        answerMap: HashMap<ProductAutomatonTuple, Double>
    ): HashMap<PairOfStrings, Double> {
        return HashMap<PairOfStrings, Double>().apply {
            answerMap.map { (key, value) ->
                val newKey = PairOfStrings(
                    key.sourceProductAutomatonNode!!.identifier.third.identifier,
                    key.targetProductAutomatonNode.identifier.third.identifier
                )
                put(newKey, value)
            }
        }
    }


}