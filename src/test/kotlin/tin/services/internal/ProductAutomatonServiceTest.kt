package tin.services.internal

import org.junit.jupiter.api.Test
import tin.model.dataProvider.DataProvider
import tin.model.database.DatabaseGraph
import tin.model.database.DatabaseNode
import tin.model.productAutomaton.ProductAutomatonEdgeType
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.productAutomaton.ProductAutomatonNode
import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import tin.model.transducer.TransducerGraph
import tin.model.transducer.TransducerNode
import tin.services.internal.fileReaders.DatabaseReaderService
import tin.services.internal.fileReaders.QueryReaderService
import tin.services.internal.fileReaders.TransducerReaderService
import tin.services.technical.SystemConfigurationServiceTest

class ProductAutomatonServiceTest {

    private var systemConfigurationService: SystemConfigurationServiceTest = SystemConfigurationServiceTest()

    private var productAutomatonService: ProductAutomatonService = ProductAutomatonService()

    @Test
    fun constructProductAutomaton() {
        // goal: test all 9 edge types separately.
        // therefore: create various small queries and transducers, database should be no problem

        // case: EpsilonIncomingPositiveOutgoing
        val comparingProductAutomatonGraph =
            constructComparisonGraph(ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing)
        val productAutomatonGraph =
            productAutomatonService.constructProductAutomaton(createDataProvider(ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing))
        assert(productAutomatonGraph == comparingProductAutomatonGraph)

        // case: EpsilonIncomingNegativeOutgoing
        // case: EpsilonIncomingEpsilonOutgoing
        // case: PositiveIncomingPositiveOutgoing
        // case: PositiveIncomingNegativeOutgoing
        // case: PositiveIncomingEpsilonOutgoing
        // case: NegativeIncomingPositiveOutgoing
        // case: NegativeIncomingNegativeOutgoing
        // case: NegativeIncomingEpsilonOutgoing

    }

    private fun constructComparisonGraph(edgeType: ProductAutomatonEdgeType): ProductAutomatonGraph {
        val graph = ProductAutomatonGraph()

        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                /** init query data */
                val queryGraph = QueryGraph()
                queryGraph.addQueryNodes(QueryNode("q0", isInitialState = true, isFinalState = true))
                val q0 = queryGraph.nodes.find { it.identifier == "q0" }!!
                queryGraph.addQueryEdge(q0,q0,"epsilon")

                /** init transducer data */
                val transducerGraph = TransducerGraph()
                transducerGraph.addNodes(TransducerNode("t0", initialState = true, finalState = true))
                val t0 = transducerGraph.nodes.find { it.identifier == "t0" }!!
                transducerGraph.addEdge(t0, t0, "epsilon", "l1", 0.0)
                transducerGraph.addEdge(t0, t0, "epsilon", "l2", 3.0)
                transducerGraph.addEdge(t0, t0, "epsilon", "l3", 5.0)

                /** init database data */
                val databaseGraph = DatabaseGraph()
                databaseGraph.addNodes(
                    DatabaseNode("d0"),
                    DatabaseNode("d1"),
                    DatabaseNode("d2"),
                    DatabaseNode("d3"),
                )

                val d0 = databaseGraph.nodes.find { it.identifier == "d0" }!!
                val d1 = databaseGraph.nodes.find { it.identifier == "d1" }!!
                val d2 = databaseGraph.nodes.find { it.identifier == "d2" }!!
                val d3 = databaseGraph.nodes.find { it.identifier == "d3" }!!

                databaseGraph.addEdge(source = d0, target = d1, label = "l1")
                databaseGraph.addEdge(source = d1, target = d2, label = "l2")
                databaseGraph.addEdge(source = d2, target = d3, label = "l3")

                graph.addProductAutomatonEdge(
                    source = ProductAutomatonNode(
                        queryNode = q0,
                        transducerNode = t0,
                        databaseNode = d2,
                        initialState = true,
                        finalState = true,
                    ),
                    target = ProductAutomatonNode(
                        queryNode = q0,
                        transducerNode = t0,
                        databaseNode = d3,
                        initialState = true,
                        finalState = true,
                    ),
                    incoming = "epsilon",
                    outgoing = "l3",
                    cost = 5.0,
                )

                graph.addProductAutomatonEdge(
                    source = ProductAutomatonNode(
                        queryNode = q0,
                        transducerNode = t0,
                        databaseNode = d1,
                        initialState = true,
                        finalState = true,
                    ),
                    target = ProductAutomatonNode(
                        queryNode = q0,
                        transducerNode = t0,
                        databaseNode = d2,
                        initialState = true,
                        finalState = true,
                    ),
                    incoming = "epsilon",
                    outgoing = "l2",
                    cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                    source = ProductAutomatonNode(
                        queryNode = q0,
                        transducerNode = t0,
                        databaseNode = d0,
                        initialState = true,
                        finalState = true,
                    ),
                    target = ProductAutomatonNode(
                        queryNode = q0,
                        transducerNode = t0,
                        databaseNode = d1,
                        initialState = true,
                        finalState = true,
                    ),
                    incoming = "epsilon",
                    outgoing = "l1",
                    cost = 0.0,
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                TODO()
            }
        }

        return graph

    }

    private fun createDataProvider(edgeTestCase: ProductAutomatonEdgeType): DataProvider {
        return when (edgeTestCase) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing)
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing)
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing)
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing)
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing)
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing)
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing)
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing)
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                buildDataProvider(ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing)
            }
        }
    }

    private fun buildDataProvider(edgeTestCase: ProductAutomatonEdgeType): DataProvider {

        val queryFilename: String
        val transducerFilename: String
        val databaseFilename: String

        when (edgeTestCase) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                queryFilename = "epsilonIncomingPositiveOutgoingQuery.txt"
                transducerFilename = "epsilonIncomingPositiveOutgoingTransducer.txt"
                databaseFilename = "test_db.txt"
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                TODO()
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                TODO()
            }
        }

        val queryReaderService = QueryReaderService()
        val transducerReaderService = TransducerReaderService()
        val databaseReaderService = DatabaseReaderService()

        val queryGraph =
            queryReaderService.readRegularPathQueryFile(systemConfigurationService.uploadPathForQueries + queryFilename)
        val databaseGraph =
            databaseReaderService.readDatabaseFile(systemConfigurationService.uploadPathForDatabases + databaseFilename)
        val transducerGraph =
            transducerReaderService.readTransducerFile(systemConfigurationService.uploadPathForTransducers + transducerFilename)

        val alphabet = queryGraph.alphabet.plus(databaseGraph.alphabet)

        return DataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)
    }
}