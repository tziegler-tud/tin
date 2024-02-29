package tin.services.internal

import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service
import tin.model.alphabet.Alphabet
import tin.model.dataProvider.RegularPathQueryDataProvider
import tin.model.database.DatabaseGraph
import tin.model.database.DatabaseNode
import tin.model.productAutomaton.ProductAutomatonEdgeType
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.productAutomaton.ProductAutomatonNode
import tin.model.query.QueryGraph
import tin.model.query.QueryNode
import tin.model.transducer.TransducerGraph
import tin.model.transducer.TransducerNode
import tin.services.technical.SystemConfigurationService

@Service
class ProductAutomatonServiceTest {

    private var systemConfigurationService: SystemConfigurationService = SystemConfigurationService()

    private var productAutomatonService: ProductAutomatonService = ProductAutomatonService()

    private val databaseGraph = constructTestDatabaseGraph()

    private val alphabet = Alphabet()

    private fun printResult(productAutomatonGraph: ProductAutomatonGraph, comparisonGraph: ProductAutomatonGraph) {
        println("comparing graphs: constructed / comparison graph:\n")
        productAutomatonGraph.printGraph()
        println("\n")
        comparisonGraph.printGraph()
    }


    // goal: test all 9 edge types separately.
    // therefore: create various small queries and transducers, database should be no problem
    // we use the same db for all cases

    //separate tests for each edge type, makes it easier to identify where the problem is

    //how to test:
    //1. create a matching query and transducer graph for the respective case
    //2. Build the comparison graph manually
    //3. Use ProductAutomatonService to construct productGraph
    //4. Compare these two graphs.

    @Test
    fun epsilonIncomingPositiveOutgoing() {
        val edgeType = ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing
        /** init query data */
        println("Testing product automaton construction for: $edgeType")
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)
    }

    @Test
    fun epsilonIncomingNegativeOutgoing() {
        val edgeType = ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)

        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun epsilonIncomingEpsilonOutgoing() {
        val edgeType = ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun epsilonIncomingPropertyOutgoing() {
        val edgeType = ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)
        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PositiveIncomingPositiveOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PositiveIncomingNegativeOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PositiveIncomingEpsilonOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PositiveIncomingPropertyOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun NegativeIncomingPositiveOutgoing() {
        val edgeType = ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun NegativeIncomingNegativeOutgoing() {
        val edgeType = ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        println("comparing graphs: constructed / comparison graph:\n")
        productAutomatonGraph.printGraph()
        comparisonGraph.printGraph()
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun NegativeIncomingEpsilonOutgoing() {
        val edgeType = ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun NegativeIncomingPropertyOutgoing() {
        val edgeType = ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)
        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PropertyIncomingEpsilonOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)

        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PropertyIncomingPositiveOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)

        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PropertyIncomingNegativeOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)

        assert(productAutomatonGraph == comparisonGraph)

    }

    @Test
    fun PropertyIncomingPropertyOutgoing() {
        val edgeType = ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing
        println("Testing product automaton construction for: $edgeType")

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraph(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        printResult(productAutomatonGraph, comparisonGraph)

        assert(productAutomatonGraph == comparisonGraph)

    }


    @Test
    fun epsilonIncomingPositiveOutgoingFailing(){
        val edgeType = ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing

        /** init query data */
        val queryGraph = constructTestQueryGraph(edgeType)

        /** init transducer data */
        val transducerGraph = constructTestTransducerGraph(edgeType)

        val dataProvider = buildDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)

        val comparisonGraph = constructComparisonGraphFailing(edgeType, queryGraph, transducerGraph, databaseGraph)

        val productAutomatonGraph = productAutomatonService.constructProductAutomaton(dataProvider)

        assert(productAutomatonGraph !== comparisonGraph)
    }

    private fun constructTestDatabaseGraph(): DatabaseGraph {
        val databaseGraph = DatabaseGraph()
        val d0 = DatabaseNode("d0")
        val d1 = DatabaseNode("d1")
        val d2 = DatabaseNode("d2")
        val d3 = DatabaseNode("d3")

        databaseGraph.addNodes(
                d0, d1, d2, d3
        )

        databaseGraph.addNodeProperty(d0, "prop1")
        databaseGraph.addNodeProperty(d1, "prop2")
        databaseGraph.addNodeProperty(d2, "prop3")
        databaseGraph.addNodeProperty(d3, "prop1")
        databaseGraph.addNodeProperty(d3, "prop2")
        databaseGraph.addNodeProperty(d3, "prop3")

        databaseGraph.addEdge(source = d0, target = d1, label = "l1")
        databaseGraph.addEdge(source = d1, target = d2, label = "l2")
        databaseGraph.addEdge(source = d0, target = d3, label = "l3")

        return databaseGraph
    }


    private fun constructTestQueryGraph(edgeType: ProductAutomatonEdgeType): QueryGraph {
        val graph: QueryGraph = QueryGraph()
        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing
            -> {
                graph.addNodes(QueryNode("q0", isInitialState = true, isFinalState = true))
                val q0 = graph.nodes.find { it.identifier == "q0" }!!
                graph.addEdge(q0, q0, "epsilon")
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing -> {
                graph.addNodes(QueryNode("q0", isInitialState = true, isFinalState = true))
                val q0 = graph.nodes.find { it.identifier == "q0" }!!
                graph.addEdge(q0, q0, "l1")
                graph.addEdge(q0, q0, "l2")
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing -> {
                graph.addNodes(QueryNode("q0", isInitialState = true, isFinalState = true))
                val q0 = graph.nodes.find { it.identifier == "q0" }!!
                graph.addEdge(q0, q0, "-l1")
                graph.addEdge(q0, q0, "-l2")
            }

            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> {
                graph.addNodes(QueryNode("q0", isInitialState = true, isFinalState = true))
                val q0 = graph.nodes.find { it.identifier == "q0" }!!
                graph.addEdge(q0, q0, "prop1?")
                graph.addEdge(q0, q0, "prop2?")
            }
        }

        return graph
    }

    private fun constructTestTransducerGraph(edgeType: ProductAutomatonEdgeType): TransducerGraph {
        val graph: TransducerGraph = TransducerGraph()
        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "epsilon", "l1", 0.0)
                graph.addEdge(t0, t0, "epsilon", "l2", 3.0)
                graph.addEdge(t0, t0, "epsilon", "l3", 5.0)
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "epsilon", "-l1", 0.0)
                graph.addEdge(t0, t0, "epsilon", "-l2", 3.0)
                graph.addEdge(t0, t0, "epsilon", "-l3", 5.0)
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "epsilon", "epsilon", 3.0)
            }

            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "epsilon", "prop1?", 0.0)
                graph.addEdge(t0, t0, "epsilon", "prop2?", 3.0)
                graph.addEdge(t0, t0, "epsilon", "prop3?", 5.0)
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "l1", "l1", 0.0)
                graph.addEdge(t0, t0, "l1", "l2", 3.0)
                graph.addEdge(t0, t0, "l1", "l3", 5.0)
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "l1", "-l1", 0.0)
                graph.addEdge(t0, t0, "l1", "-l2", 3.0)
                graph.addEdge(t0, t0, "l1", "-l3", 5.0)
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "l1", "epsilon", 3.0)
            }

            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "l1", "prop1?", 0.0)
                graph.addEdge(t0, t0, "l1", "prop2?", 3.0)
                graph.addEdge(t0, t0, "l2", "prop2?", 5.0)
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "-l1", "l1", 0.0)
                graph.addEdge(t0, t0, "-l1", "l2", 3.0)
                graph.addEdge(t0, t0, "-l2", "l3", 5.0)
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "-l1", "-l1", 0.0)
                graph.addEdge(t0, t0, "-l1", "-l2", 3.0)
                graph.addEdge(t0, t0, "-l2", "-l2", 5.0)
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "-l1", "epsilon", 0.0)
                graph.addEdge(t0, t0, "-l1", "epsilon", 3.0)
                graph.addEdge(t0, t0, "-l2", "epsilon", 5.0)
            }

            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "-l1", "prop1?", 0.0)
                graph.addEdge(t0, t0, "-l1", "prop2?", 3.0)
                graph.addEdge(t0, t0, "-l2", "prop2?", 5.0)
            }

            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "prop1?", "epsilon", 0.0)
                graph.addEdge(t0, t0, "prop1?", "epsilon", 3.0)
                graph.addEdge(t0, t0, "prop2?", "epsilon", 5.0)
            }

            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "prop1?", "l1", 0.0)
                graph.addEdge(t0, t0, "prop1?", "l2", 3.0)
                graph.addEdge(t0, t0, "prop2?", "l2", 5.0)
            }

            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "prop1?", "-l1", 0.0)
                graph.addEdge(t0, t0, "prop1?", "-l2", 3.0)
                graph.addEdge(t0, t0, "prop2?", "-l2", 5.0)
            }

            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> {
                graph.addNodes(TransducerNode("t0", isInitialState = true, isFinalState = true))
                val t0 = graph.nodes.find { it.identifier == "t0" }!!
                graph.addEdge(t0, t0, "prop1?", "prop1?", 0.0)
                graph.addEdge(t0, t0, "prop1?", "prop2?", 3.0)
                graph.addEdge(t0, t0, "prop2?", "prop2?", 5.0)
            }
        }

        return graph
    }

    private fun constructComparisonGraph(edgeType: ProductAutomatonEdgeType, queryGraph: QueryGraph, transducerGraph: TransducerGraph, databaseGraph: DatabaseGraph): ProductAutomatonGraph {
        val graph = ProductAutomatonGraph()

        val q0 = queryGraph.getNode("q0")!!
        val t0 = transducerGraph.getNode("t0")!!

        val d0 = databaseGraph.getNode("d0")!!
        val d1 = databaseGraph.getNode("d1")!!
        val d2 = databaseGraph.getNode("d2")!!
        val d3 = databaseGraph.getNode("d3")!!

        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {

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
                                databaseNode = d3,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "l3",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "-l1",
                        cost = 0.0,
                )

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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "-l2",
                        cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
                                initialState = true,
                                finalState = true,
                        ),
                        target = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "-l3",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "epsilon",
                        cost = 3.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "epsilon",
                        cost = 3.0,
                )

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
                                databaseNode = d2,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "epsilon",
                        cost = 3.0,
                )
                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        outgoing = "epsilon",
                        cost = 3.0,
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "prop1?",
                        cost = 0.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        outgoing = "prop1?",
                        cost = 0.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "prop2?",
                        cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        outgoing = "prop2?",
                        cost = 3.0,
                )

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
                                databaseNode = d2,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "epsilon",
                        outgoing = "prop3?",
                        cost = 5.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        outgoing = "prop3?",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
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
                        incoming = "l1",
                        outgoing = "l1",
                        cost = 0.0,
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
                        incoming = "l1",
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
                                databaseNode = d3,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "l3",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "-l1",
                        cost = 0.0,
                )

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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "-l2",
                        cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
                                initialState = true,
                                finalState = true,
                        ),
                        target = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "-l3",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "epsilon",
                        cost = 3.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "epsilon",
                        cost = 3.0,
                )

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
                                databaseNode = d2,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "epsilon",
                        cost = 3.0,
                )
                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "l1",
                        outgoing = "epsilon",
                        cost = 3.0,
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "prop1?",
                        cost = 0.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "l1",
                        outgoing = "prop1?",
                        cost = 0.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l1",
                        outgoing = "prop2?",
                        cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "l1",
                        outgoing = "prop2?",
                        cost = 3.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "l2",
                        outgoing = "prop2?",
                        cost = 5.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "l2",
                        outgoing = "prop2?",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
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
                        incoming = "-l1",
                        outgoing = "l1",
                        cost = 0.0,
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
                        incoming = "-l1",
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
                                databaseNode = d3,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l2",
                        outgoing = "l3",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l1",
                        outgoing = "-l1",
                        cost = 0.0,
                )

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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l1",
                        outgoing = "-l2",
                        cost = 3.0,
                )

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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l2",
                        outgoing = "-l2",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {

                val databaseNodes = arrayOf(d0, d1, d2, d3)
                databaseNodes.forEach {
                    graph.addProductAutomatonEdge(
                            source = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            target = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            incoming = "-l1",
                            outgoing = "epsilon",
                            cost = 0.0,
                    )

                    graph.addProductAutomatonEdge(
                            source = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            target = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            incoming = "-l1",
                            outgoing = "epsilon",
                            cost = 3.0,
                    )

                    graph.addProductAutomatonEdge(
                            source = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            target = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            incoming = "-l2",
                            outgoing = "epsilon",
                            cost = 5.0,
                    )
                }

            }

            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l1",
                        outgoing = "prop1?",
                        cost = 0.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "-l1",
                        outgoing = "prop1?",
                        cost = 0.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l1",
                        outgoing = "prop2?",
                        cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "-l1",
                        outgoing = "prop2?",
                        cost = 3.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "-l2",
                        outgoing = "prop2?",
                        cost = 5.0,
                )
                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "-l2",
                        outgoing = "prop2?",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing -> {

                val databaseNodes = arrayOf(d0, d1, d2, d3)
                databaseNodes.forEach {
                    graph.addProductAutomatonEdge(
                            source = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            target = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            incoming = "prop1?",
                            outgoing = "epsilon",
                            cost = 0.0,
                    )
                    graph.addProductAutomatonEdge(
                            source = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            target = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            incoming = "prop1?",
                            outgoing = "epsilon",
                            cost = 3.0,
                    )
                    graph.addProductAutomatonEdge(
                            source = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            target = ProductAutomatonNode(
                                    queryNode = q0,
                                    transducerNode = t0,
                                    databaseNode = it,
                                    initialState = true,
                                    finalState = true,
                            ),
                            incoming = "prop2?",
                            outgoing = "epsilon",
                            cost = 5.0,
                    )
                }

            }

            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing -> {
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
                        incoming = "prop1?",
                        outgoing = "l1",
                        cost = 0.0,
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
                        incoming = "prop1?",
                        outgoing = "l2",
                        cost = 3.0,
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
                        incoming = "prop2?",
                        outgoing = "l2",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "prop1?",
                        outgoing = "-l1",
                        cost = 0.0,
                )

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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "prop1?",
                        outgoing = "-l2",
                        cost = 3.0,
                )

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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "prop2?",
                        outgoing = "-l2",
                        cost = 5.0,
                )
            }

            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> {
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
                                databaseNode = d0,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "prop1?",
                        outgoing = "prop1?",
                        cost = 0.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "prop1?",
                        outgoing = "prop1?",
                        cost = 0.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "prop1?",
                        outgoing = "prop2?",
                        cost = 3.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "prop1?",
                        outgoing = "prop2?",
                        cost = 3.0,
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
                                databaseNode = d1,
                                initialState = true,
                                finalState = true,
                        ),
                        incoming = "prop2?",
                        outgoing = "prop2?",
                        cost = 5.0,
                )

                graph.addProductAutomatonEdge(
                        source = ProductAutomatonNode(
                                queryNode = q0,
                                transducerNode = t0,
                                databaseNode = d3,
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
                        incoming = "prop2?",
                        outgoing = "prop2?",
                        cost = 5.0,
                )
            }
        }

        return graph

    }

    private fun constructComparisonGraphFailing(edgeType: ProductAutomatonEdgeType, queryGraph: QueryGraph, transducerGraph: TransducerGraph, databaseGraph: DatabaseGraph): ProductAutomatonGraph {
        val graph = ProductAutomatonGraph()

        val q0 = queryGraph.getNode("q0")!!
        val t0 = transducerGraph.getNode("t0")!!

        val d0 = databaseGraph.getNode("d0")!!
        val d1 = databaseGraph.getNode("d1")!!
        val d2 = databaseGraph.getNode("d2")!!
        val d3 = databaseGraph.getNode("d3")!!

        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {

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
                                databaseNode = d0,
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
                        cost = 3.0,
                )
            }

            else -> {
                //Test cases not implemented
            }
        }

        return graph

    }

//    private fun createDataProvider(edgeTestCase: ProductAutomatonEdgeType): DataProvider {
//        //we build the graphs here manually, as we do not want to test the input file readers at this point
//
//        return when (edgeTestCase) {
//            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing)
//            }
//
//            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing)
//            }
//
//            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing)
//            }
//
//            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing)
//            }
//
//            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing)
//            }
//
//            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing)
//            }
//
//            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing)
//            }
//
//            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing)
//            }
//
//            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
//                buildDataProvider(ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing)
//            }
//
//            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing -> TODO()
//            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing -> TODO()
//            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing -> TODO()
//            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing -> TODO()
//            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing -> TODO()
//            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing -> TODO()
//            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> TODO()
//        }
//    }

    private fun buildDataProvider(queryGraph: QueryGraph, transducerGraph: TransducerGraph, databaseGraph: DatabaseGraph, alphabet: Alphabet): RegularPathQueryDataProvider {
        return RegularPathQueryDataProvider(queryGraph, transducerGraph, databaseGraph, alphabet)
    }
}