package tin.services.internal.queryAnswering

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.model.alphabet.Alphabet
import tin.model.dataProvider.OntologyQueryDataProvider
import tin.model.utils.ProductAutomatonTuple
import tin.model.dataProvider.RegularPathQueryDataProvider
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.query.QueryGraph
import tin.model.queryResult.RegularPathQueryResult
import tin.model.queryResult.QueryResultRepository
import tin.model.queryTask.QueryTask
import tin.model.queryTask.QueryTaskRepository
import tin.model.queryTask.ComputationProperties
import tin.model.queryResult.computationStatistics.ComputationStatistics
import tin.model.queryResult.QueryResult
import tin.model.queryResult.QueryResultStatus
import tin.model.queryResult.computationStatistics.RegularPathComputationStatistics
import tin.model.tintheweb.FileRepository
import tin.model.transducer.TransducerGraph
import tin.services.internal.ProductAutomatonService
import tin.services.internal.dijkstra.DijkstraQueryAnsweringUtils
import tin.services.internal.dijkstra.algorithms.Dijkstra
import tin.services.internal.dijkstra.algorithms.DijkstraThreshold
import tin.services.internal.dijkstra.algorithms.DijkstraTopK
import tin.services.internal.utils.TransducerFactory
import tin.services.technical.SystemConfigurationService
import tin.utils.findByIdentifier
import tin.services.internal.fileReaders.DatabaseReaderService
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.QueryReaderService
import tin.services.internal.fileReaders.TransducerReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyManager
import kotlin.system.measureNanoTime

@Service
class DLRegularPathQueryAnsweringService(
    private val fileRepository: FileRepository,
    private val queryTaskRepository: QueryTaskRepository,
    private val queryResultRepository: QueryResultRepository,

    ) {
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    @Autowired
    lateinit var queryReaderService: QueryReaderService

    @Autowired
    lateinit var ontologyReaderService: OntologyReaderService

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


        //TODO: Call subservices to process query

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

    private fun buildDataProvider(data: QueryTask): OntologyQueryDataProvider {

        // find files
        val queryFileDb = fileRepository.findByIdentifier(data.queryFileIdentifier)
        val ontologyFileDb = fileRepository.findByIdentifier(data.dataSourceFileIdentifier)

        val queryReaderResult: FileReaderResult<QueryGraph> =
            queryReaderService.read(systemConfigurationService.getQueryPath(), queryFileDb.filename)
        val queryGraph = queryReaderResult.get()

        val ontologyReaderResult =
            ontologyReaderService.read(systemConfigurationService.getOntologyPath(), ontologyFileDb.filename)
        val ontologyFile = ontologyReaderResult.get()



        val ontologyManager = OntologyManager(ontologyFile)
        val ontologyAlphabet = ontologyManager.getAlphabet();

        val transducerGraph: TransducerGraph
        val alphabet = Alphabet(queryGraph.alphabet)
        alphabet.addAlphabet(ontologyAlphabet)


        if (data.computationProperties.generateTransducer && data.computationProperties.transducerGeneration != null) {
            // generate transducer
            transducerGraph = when (data.computationProperties.transducerGeneration) {
                ComputationProperties.TransducerGeneration.ClassicalAnswersPreserving -> TransducerFactory.generateClassicAnswersTransducer(
                        alphabet
                )

                ComputationProperties.TransducerGeneration.EditDistance -> TransducerFactory.generateEditDistanceTransducer(
                        queryGraph.alphabet, ontologyAlphabet
                )
            }
        } else {
            // transducer file is provided -> no generation needed
            val transducerFileDb = fileRepository.findByIdentifier(data.transducerFileIdentifier!!)
            val transducerReaderResult =
                transducerReaderService.read(systemConfigurationService.getTransducerPath(), transducerFileDb.filename)
            transducerGraph = transducerReaderResult.get()
        }

        return OntologyQueryDataProvider(
            queryGraph = queryGraph,
            transducerGraph = transducerGraph,
            ontologyManager = ontologyManager,
            sourceVariableName = null,
            targetVariableName = null,
            alphabet = alphabet
        )
    }
}