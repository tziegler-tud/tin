package tin.services.internal

import tin.data.CombinedQueryData
import tin.data.ComputationMode
import tin.model.utils.ProductAutomatonTuple
import tin.data.QueryResultsData
import tin.data.StringPairData
import tin.data.input.GenerateTransducer
import tin.model.DataProvider
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.transducer.TransducerGraph
import tin.services.internal.algorithms.Dijkstra
import tin.services.internal.algorithms.DijkstraThreshold
import tin.services.internal.algorithms.DijkstraTopK
import tinput.services.individualFiles.DatabaseReader
import tinput.services.individualFiles.QueryReader
import tinput.services.individualFiles.TransducerReader
import kotlin.system.measureNanoTime

class DijkstraQueryAnsweringService {

    fun calculateQuery(data: CombinedQueryData): QueryResultsData {

        val queryResultsData: QueryResultsData
        val dataProvider = buildDataProvider(data)

        queryResultsData = when (data.computationMode) {
            ComputationMode.Dijkstra -> calculateDijkstra(dataProvider)
            ComputationMode.Threshold -> calculateThreshold(data, dataProvider)
            ComputationMode.TopK -> calculateTopK(data, dataProvider)
        }

        return queryResultsData
    }

    private fun buildDataProvider(data: CombinedQueryData): DataProvider {

        val queryReader = QueryReader()
        val transducerReader = TransducerReader()
        val databaseReader = DatabaseReader()

        val queryGraph = queryReader.readRegularPathQueryFile(data.queryData.queryFile)
        val databaseGraph = databaseReader.readDatabaseFile(data.queryData.databaseFile)

        val transducerGraph: TransducerGraph
        val alphabet = queryGraph.alphabet.plus(databaseGraph.alphabet)

        transducerGraph = when (data.queryData.generateTransducer) {
            GenerateTransducer.NoGeneration -> transducerReader.readTransducerFile(data.queryData.transducerFile)
            GenerateTransducer.ClassicAnswers -> transducerReader.generateClassicAnswersTransducer(alphabet)
            GenerateTransducer.EditDistance -> transducerReader.generateEditDistanceTransducer(alphabet)
        }

        return DataProvider(queryGraph, transducerGraph, databaseGraph)
    }

    private fun calculateDijkstra(dataProvider: DataProvider): QueryResultsData {

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

        return QueryResultsData(preprocessingTime, mainProcessingTime, makeAnswerMapReadable(answerMap))
    }

    private fun calculateThreshold(data: CombinedQueryData, dataProvider: DataProvider): QueryResultsData {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstra = DijkstraThreshold(productAutomatonGraph, data.computationProperties.threshold!!)
            answerMap = dijkstra.processDijkstraOverAllInitialNodes()
        }

        return QueryResultsData(preprocessingTime, mainProcessingTime, makeAnswerMapReadable(answerMap))
    }

    private fun calculateTopK(data: CombinedQueryData, dataProvider: DataProvider): QueryResultsData {

        val productAutomatonService = ProductAutomatonService()
        val productAutomatonGraph: ProductAutomatonGraph
        val answerMap: HashMap<ProductAutomatonTuple, Double>

        val preprocessingTime = measureNanoTime {
            productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        }

        val mainProcessingTime = measureNanoTime {
            val dijkstra = DijkstraTopK(productAutomatonGraph, data.computationProperties.topK!!)
            answerMap = dijkstra.processDijkstraOverAllInitialNodes()
        }

        return QueryResultsData(preprocessingTime, mainProcessingTime, makeAnswerMapReadable(answerMap))
    }

    private fun makeAnswerMapReadable(
        answerMap: HashMap<ProductAutomatonTuple, Double>
    ): HashMap<StringPairData, Double> {
        return HashMap<StringPairData, Double>().apply {
            answerMap.map { (key, value) ->
                val newKey = StringPairData(
                    key.sourceProductAutomatonNode!!.identifier.third.identifier,
                    key.targetProductAutomatonNode.identifier.third.identifier
                )
                newKey to value
            }
        }
    }


}