package tin.services.internal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.data.tintheweb.queryResult.AnswerTripletData
import tin.model.utils.ProductAutomatonTuple
import tin.data.tintheweb.queryResult.ComputationStatisticsData
import tin.model.dataProvider.DataProvider
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.queryResult.QueryResult
import tin.model.queryResult.QueryResultRepository
import tin.model.queryTask.ComputationMode
import tin.model.queryTask.QueryTask
import tin.model.queryTask.QueryTaskRepository
import tin.model.queryTask.ComputationProperties
import tin.model.queryResult.ComputationStatistics
import tin.model.tintheweb.FileRepository
import tin.model.transducer.TransducerGraph
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
    private val queryResultRepository: QueryResultRepository,
) {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    @Transactional
    fun calculateQueryTask(queryTask: QueryTask): QueryResult {
        // set status to calculating
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Calculating }
        queryTaskRepository.save(queryTask)

        val dataProvider = buildDataProvider(queryTask)
        var pairContainingCompStatsAndAnswerSet: Pair<ComputationStatistics, Set<QueryResult.AnswerTriplet>>? = null

        var queryResultStatus: QueryResult.QueryResultStatus = QueryResult.QueryResultStatus.NoError


        when (queryTask.computationMode.computationModeEnum) {
            ComputationMode.ComputationModeEnum.Dijkstra -> pairContainingCompStatsAndAnswerSet =
                calculateDijkstra(dataProvider)

            ComputationMode.ComputationModeEnum.Threshold -> if (queryTask.computationMode.computationProperties.thresholdValue == null) {
                queryResultStatus = QueryResult.QueryResultStatus.ErrorInComputationMode
            } else {
                pairContainingCompStatsAndAnswerSet = calculateThreshold(
                    dataProvider, queryTask.computationMode.computationProperties.thresholdValue
                )
            }

            ComputationMode.ComputationModeEnum.TopK -> if (queryTask.computationMode.computationProperties.topKValue == null) {
                queryResultStatus = QueryResult.QueryResultStatus.ErrorInComputationMode
            } else {
                pairContainingCompStatsAndAnswerSet =
                    calculateTopK(dataProvider, queryTask.computationMode.computationProperties.topKValue)
            }
        }

        val queryResult =

            // return if an error was found
            if (queryResultStatus != QueryResult.QueryResultStatus.NoError) {
                QueryResult(
                    queryTask,
                    null,
                    queryResultStatus,
                    HashSet()
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
                    pairContainingCompStatsAndAnswerSet.second
                )
            }

        // set status to finished
        queryTask.apply { queryStatus = QueryTask.QueryStatus.Finished }
        queryTaskRepository.save(queryTask)

        // save the queryResult
        queryResultRepository.save(queryResult)

        return queryResult
    }

    private fun buildDataProvider(data: QueryTask): DataProvider {

        // find files
        val queryFileDb = fileRepository.findByIdentifier(data.queryFileIdentifier)
        val databaseFileDb = fileRepository.findByIdentifier(data.databaseFileIdentifier)

        val queryReader = QueryReader()
        val transducerReader = TransducerReader()
        val databaseReader = DatabaseReader()

        val queryGraph =
            queryReader.readRegularPathQueryFile(systemConfigurationService.uploadPathForQueries + "/" + queryFileDb.filename)
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
    fun calculateDijkstra(dataProvider: DataProvider): Pair<ComputationStatistics, Set<QueryResult.AnswerTriplet>> {

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

        val transformedAnswerSet: Set<QueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            transformedAnswerSet = makeAnswerMapReadable(answerMap)
        }

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            ComputationStatistics(
                preprocessingTime / 1000000, mainProcessingTime / 1000000, postProcessingTime / 1000000
            ), transformedAnswerSet
        )
    }

    @Transactional
    fun calculateThreshold(
        dataProvider: DataProvider, threshold: Double
    ): Pair<ComputationStatistics, Set<QueryResult.AnswerTriplet>> {

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

        val transformedAnswerSet: Set<QueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            transformedAnswerSet = makeAnswerMapReadable(answerMap)
        }

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            ComputationStatistics(
                preprocessingTime / 1000000, mainProcessingTime / 1000000, postProcessingTime / 1000000
            ), transformedAnswerSet
        )
    }

    @Transactional
    fun calculateTopK(
        dataProvider: DataProvider,
        kValue: Int
    ): Pair<ComputationStatistics, Set<QueryResult.AnswerTriplet>> {

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

        val transformedAnswerSet: Set<QueryResult.AnswerTriplet>
        val postProcessingTime = measureNanoTime {
            transformedAnswerSet = makeAnswerMapReadable(answerMap)
        }

        // we store milliseconds instead of nanoseconds, hence we need to 10^-6 all processingTimes
        return Pair(
            ComputationStatistics(
                preprocessingTime / 1000000, mainProcessingTime / 1000000, postProcessingTime / 1000000
            ), transformedAnswerSet
        )
    }

    /**
     * transforms internal answerMap containing a (source, target) ProductAutomatonTuple as key, and a Double as value (cost of reaching target from source)
     * into a set of AnswerTriplets (source.name, target.name, double); omitting the technical ProductAutomatonNodes
     * after finishing the query we do not care about technical details, we simply want the (human-readable) results.
     */
    private fun makeAnswerMapReadable(
        answerMap: HashMap<ProductAutomatonTuple, Double>
    ): Set<QueryResult.AnswerTriplet> {
        return HashSet<QueryResult.AnswerTriplet>().apply {
            answerMap.forEach { (key, value) ->
                val source = key.sourceProductAutomatonNode!!.identifier.third.identifier
                val target = key.targetProductAutomatonNode.identifier.third.identifier
                add(QueryResult.AnswerTriplet(source, target, value))
            }
        }
    }


}