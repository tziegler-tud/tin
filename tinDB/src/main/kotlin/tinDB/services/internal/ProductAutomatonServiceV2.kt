package tinDB.services.internal

import tinDB.model.v2.DatabaseGraph.DatabaseEdge
import tinDB.model.v2.DatabaseGraph.DatabaseEdgeLabel
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v2.productAutomaton.*

import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.transducer.TransducerEdge

class ProductAutomatonServiceV2(
    regularPathQueryDataProvider: RegularPathQueryDataProvider
) {
    companion object {
        val epsilonLabelString = "epsilon"
    }

    private val databaseEpsilonLabel = DatabaseEdgeLabel(ProductAutomatonServiceV2.epsilonLabelString)
    private val dataProvider = regularPathQueryDataProvider
    private var productAutomatonGraph = ProductAutomatonGraph()

    private val transducerGraph = regularPathQueryDataProvider.transducerGraph
    private val queryGraph = regularPathQueryDataProvider.queryGraph
    private val databaseGraph = regularPathQueryDataProvider.databaseGraph

    /**
     * constructs the product automaton accordingly to the Grahne & Thomo paper (2006)
     */
    fun constructProductAutomaton(): ProductAutomatonGraph {
        val incomingEpsilonEdges = HashSet<TransducerEdge>()

        if (queryGraph.nodes.isEmpty()) {

            // find all incoming epsilon edges
            // bandaid fix to insert incoming epsilon edges to fitting transducer edge every iteration.

            transducerGraph.edges.forEach { transducerEdge ->
                val incomingLabel = transducerEdge.label.incoming
                if (incomingLabel.isEpsilonLabel()) {
                    incomingEpsilonEdges.add(transducerEdge)
                }
            }
        }



        //part (I)
        for (querySource in queryGraph.nodes) {
            for (queryTarget in queryGraph.nodes) {
                for(transducerSource in transducerGraph.nodes) {
                    transducerGraph.nodes.forEach transducerTarget@ { transducerTarget ->
                        //returns query and transducer edges that can be applied
                        val candidateTransducerEdges =
                            getFittingTransducerEdges(querySource, queryTarget, transducerSource, transducerTarget, true)
                                ?: return@transducerTarget

                        val fittingTransducerEdges: MutableList<TransducerEdge> = candidateTransducerEdges.toMutableList()
                        fittingTransducerEdges.addAll(incomingEpsilonEdges)

                        for (transducerEdge in fittingTransducerEdges) {
                            val queryEdge = QueryEdge(querySource, queryTarget, QueryEdgeLabel(transducerEdge.label.incoming))
                            // part (II)
                            // NOTE: here we also add edges of the form (IV | 2) "outgoing epsilon edges"
                            val edgeType = getEdgeType(transducerEdge)
                            if(edgeType == null) {
                                //this should not have happened
                                throw Error("ProductAutomatonService: Cannot construct ProductAutomaton Nodes: Invalid edgeType given.")
                            }

                            // PA Graph construction Steps 1-9 + 14 + 15
                            /**
                             * These are transitions in the db, i.e. they require a database edge
                             */
                            for (databaseEdge in databaseGraph.edges) {
                                //CAUTION: Below here belong ONLY cases 2,3,5,6,8,9 i.e. the ones that require a database edge!
                                val insertResult = constructProductAutomatonEdge(queryEdge, transducerEdge, databaseEdge, edgeType)
                            }

                            /**
                             * These are self-loops in the db graph, i.e. they do not require an edge
                             */
                            for (databaseNode in databaseGraph.nodes) {
                                // PA Graph construction 1,4,7,10-13,16
                                val databaseLoop = DatabaseEdge(
                                    source = databaseNode,
                                    target = databaseNode,
                                    label = databaseEpsilonLabel
                                )
                                val insertResult = constructProductAutomatonEdge(queryEdge, transducerEdge, databaseLoop, edgeType)
                            }
                        }
                    }
                }
            }
        }
        return productAutomatonGraph
    }

    /**
     * helper function that builds us the ProductAutomatonNodes needed to create new edges.
     *
     * @param queryEdge      the respective queryEdge
     * @param transducerEdge the respective transducerEdge
     * @param databaseEdge   the respective databaseEdge
     * @param edgeType       the edgeType that needs to be created.
     * @return a Pair of ProductAutomatonNodes, containing the source node (value_0) and the target node (value_1)
     */
    private fun constructAutomatonNode(
        queryEdge: QueryEdge,
        transducerEdge: TransducerEdge,
        databaseEdge: DatabaseEdge,
        edgeType: ProductAutomatonEdgeType
    ): Pair<ProductAutomatonNode, ProductAutomatonNode> {

        val resultPairOfNodes: Pair<ProductAutomatonNode, ProductAutomatonNode>
        val sourceNode: ProductAutomatonNode
        val targetNode: ProductAutomatonNode

        val initialFinalStateWrapper = getInitialFinalStateWrapper(queryEdge, transducerEdge, databaseEdge, edgeType)

        val sourceInitialState: Boolean = initialFinalStateWrapper.sourceInitialState
        val sourceFinalState: Boolean = initialFinalStateWrapper.sourceFinalState
        val targetInitialState: Boolean = initialFinalStateWrapper.targetInitialState
        val targetFinalState: Boolean = initialFinalStateWrapper.targetFinalState

        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                // epsilon incoming, positive outgoing
                // q pause, t move, db move

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.target,
                    databaseEdge.target,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
                // epsilon incoming, negative outgoing
                // q pause, t move, db move backwards

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.target,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                // epsilon incoming, epsilon outgoing
                // q pause, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                // positive incoming, positive outgoing
                // q move, t move, db move

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.target,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                // positive incoming, negative outgoing
                // q move, t move, db move backwards

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.target,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                // positive incoming, epsilon outgoing
                // q move, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                // negative incoming, positive outgoing
                // q move, t move, db move

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.target,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                // negative incoming, negative outgoing
                // q move, t move, db move backwards

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.target,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                // negative incoming, epsilon outgoing
                // q move, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing -> {
                // epsilon incoming, property outgoing
                // q pause, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing -> {
                // positive incoming, property outgoing
                // q move, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing -> {
                // negative incoming, property outgoing
                // q move, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing -> {
                //property incoming, epsilon outgoing
                // q move, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )

            }

            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing -> {
                // property incoming, positive outgoing
                // q move, t move, db move

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.target,
                    targetInitialState,
                    targetFinalState
                )
            }

            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing -> {
                // property incoming, negative outgoing
                // q move, t move, db move backwards
                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.target,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )


            }

            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> {
                // property incoming, property outgoing
                // q move, t move, db pause

                sourceNode = ProductAutomatonNode(
                    queryEdge.source,
                    transducerEdge.source,
                    databaseEdge.source,
                    sourceInitialState,
                    sourceFinalState
                )
                targetNode = ProductAutomatonNode(
                    queryEdge.target,
                    transducerEdge.target,
                    databaseEdge.source,
                    targetInitialState,
                    targetFinalState
                )
            }
        }
        resultPairOfNodes = Pair(sourceNode, targetNode)
        return resultPairOfNodes
    }


    private fun getInitialFinalStateWrapper(
        queryEdge: QueryEdge,
        transducerEdge: TransducerEdge,
        databaseEdge: DatabaseEdge,
        edgeType: ProductAutomatonEdgeType
    ): InitialFinalStateWrapper {
        var sourceInitialState: Boolean
        var sourceFinalState: Boolean
        var targetInitialState: Boolean
        var targetFinalState: Boolean

        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                // epsilon incoming, positive outgoing
                // q pause, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing -> {
                // epsilon incoming, negative outgoing
                // q pause, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                // epsilon incoming, epsilon outgoing
                // q pause, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing -> {
                // epsilon incoming, property outgoing
                // q pause, t move, db pause (we do the property check ..)
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                // positive incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                // positive incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                // positive incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing -> {
                // positive incoming, property outgoing
                // q move, t move, db pause (we do the property check ..)
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                // negative incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                // negative incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                // negative incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing -> {
                // negative incoming, property outgoing
                // q move, t move, db pause (we do the property check ..)
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing -> {
                //property incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing -> {
                // property incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing -> {
                // property incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }

            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> {
                // property incoming, property outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
            }
        }

        /**
         * Now we check for the case of C2RPQs where instead of a variable, the user put a database individual.
         * if the user put a database individual as either source or target within the atom (i.e. R1(a,b)), we have to check for the database source or target node.
         * If the corresponding node equals the individual definition in the atom, it is an initial resp. final state (else not).
         *
         * Since we have a source and target ProductAutomatonNode, we have to check if the database moves, pauses, or moves backwards.
         * The query and transducer do not matter at the moment.
         *
         */

        when (edgeType) {
            // cases where the db moves
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing -> {
                /**
                 * db moves, that means:
                 * the db source variable possibly changes the sourceInitialState
                 * the db target variable possibly changes the targetFinalState
                 */

                if (dataProvider.sourceVariableName?.contains("_individual") == true) {
                    if (databaseEdge.source.identifier != dataProvider.sourceVariableName!!.substringBefore("_individual")) {
                        // the source variable is not the individual -> it is not an initial state
                        sourceInitialState = false
                    }
                }

                if (dataProvider.targetVariableName?.contains("_individual") == true) {
                    if (databaseEdge.target.identifier != dataProvider.targetVariableName!!.substringBefore("_individual")) {
                        // the target variable is not the individual -> it is not a final state
                        targetFinalState = false
                    }
                }

            }

            // cases where the db pauses
            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing -> {
                /**
                 * db pauses, that means:
                 * the db source variable possibly changes the sourceInitialState
                 * the db source variable possibly changes the targetFinalState
                 *
                 * Note that we basically have a self loop in the database from the sourceNode to itself.
                 * (so theoretically we could also say the databaseEdge.target possibly changes the targetFinalState..)
                 */

                if (dataProvider.sourceVariableName?.contains("_individual") == true) {
                    if (databaseEdge.source.identifier != dataProvider.sourceVariableName!!.substringBefore("_individual")) {
                        // the source variable is not the individual -> it is not an initial state
                        sourceInitialState = false
                    }
                }

                if (dataProvider.targetVariableName?.contains("_individual") == true) {
                    if (databaseEdge.source.identifier != dataProvider.targetVariableName!!.substringBefore("_individual")) {
                        // the target variable is not the individual -> it is not a final state
                        targetFinalState = false
                    }
                }

            }

            // cases where the db moves backwards
            ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing,
            ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing -> {
                /**
                 * db moves backwards, that means:
                 * the db source variable possibly changes the targetInitialState
                 * the db target variable possibly changes the sourceFinalState
                 */

                if (dataProvider.sourceVariableName?.contains("_individual") == true) {
                    if (databaseEdge.source.identifier != dataProvider.sourceVariableName!!.substringBefore("_individual")) {
                        // the source variable is not the individual -> it is not an initial state
                        targetInitialState = false
                    }
                }

                if (dataProvider.targetVariableName?.contains("_individual") == true) {
                    if (databaseEdge.target.identifier != dataProvider.targetVariableName!!.substringBefore("_individual")) {
                        // the target variable is not the individual -> it is not a final state
                        sourceFinalState = false
                    }
                }
            }
        }

        return InitialFinalStateWrapper(
            sourceInitialState = sourceInitialState,
            sourceFinalState = sourceFinalState,
            targetInitialState = targetInitialState,
            targetFinalState = targetFinalState
        )
    }

    /**
     * determines the edge type based on the given transducer edge
     */
    private fun getEdgeType(transducerEdge: TransducerEdge): ProductAutomatonEdgeType? {
        var edgeType: ProductAutomatonEdgeType? = null
        if (transducerEdge.label.incoming.isEpsilonLabel()) {
            edgeType = when {
                transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                    ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing
                }
                !transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                    ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing
                }
                transducerEdge.label.outgoing.isConceptAssertion() -> {
                    ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing
                }
                transducerEdge.label.outgoing.isEpsilonLabel() -> {
                    ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing
                }
                else -> {
                    //Error
                    null
                }
            }
        } else if (transducerEdge.label.incoming.isRole()) {
            //type 3: incoming negative edge
            if(transducerEdge.label.incoming.isInverse()){
                edgeType = when {
                    transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                        ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing
                    }
                    !transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                        ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing
                    }
                    transducerEdge.label.outgoing.isConceptAssertion() -> {
                        ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing
                    }
                    transducerEdge.label.outgoing.isEpsilonLabel() -> {
                        ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing
                    }
                    else -> null
                }
            }
            //type ?: incoming positive edge
            else {
                edgeType = when {
                    transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                        ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing
                    }

                    !transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                        ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing
                    }

                    transducerEdge.label.outgoing.isConceptAssertion() -> {
                        ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing
                    }

                    transducerEdge.label.outgoing.isEpsilonLabel() -> {
                        ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing
                    }

                    else -> null

                }
            }

        } else if (transducerEdge.label.incoming.isConceptAssertion()) {
            // type 3: incoming property assertion
            edgeType = when {
                transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                    ProductAutomatonEdgeType.PropertyIncomingNegativeOutgoing
                }

                !transducerEdge.label.outgoing.isInverse() && transducerEdge.label.outgoing.isRole() -> {
                    ProductAutomatonEdgeType.PropertyIncomingPositiveOutgoing
                }

                transducerEdge.label.outgoing.isConceptAssertion() -> {
                    ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing
                }

                transducerEdge.label.outgoing.isEpsilonLabel() -> {
                    ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing
                }

                else -> null
            }

        }
        return edgeType
    }

    private fun constructProductAutomatonEdge(
        queryEdge: QueryEdge,
        transducerEdge: TransducerEdge,
        databaseEdge: DatabaseEdge,
        edgeType: ProductAutomatonEdgeType): Boolean {
        // (2)
        // for all transducer edges that were found in part (II):
        //      check whether
        val pairOfNodes = constructAutomatonNode(
            queryEdge,
            transducerEdge,
            databaseEdge,
            edgeType
        )
        val source = pairOfNodes.first
        val target = pairOfNodes.second

        val incoming = transducerEdge.label.incoming
        val outgoing = transducerEdge.label.outgoing
        val cost = transducerEdge.label.cost

        val edge = ProductAutomatonEdge(
            source,
            target,
            ProductAutomatonEdgeLabel(
                incoming,
                outgoing,
                cost
            )
        )
        return productAutomatonGraph.addEdge(edge)
    }

    private fun getFittingTransducerEdges(
        querySource: Node,
        queryTarget: Node,
        transducerSource: Node,
        transducerTarget: Node,
        includeEpsilonEdges: Boolean
    ): List<TransducerEdge>? {
        var candidateQueryEdges = queryGraph.getEdgesWithSourceAndTarget(querySource, queryTarget);
        if (candidateQueryEdges.isEmpty()) {
            return null;
        }

        var candidateQueryTransitions = candidateQueryEdges.map { it.label }

        //get all edges (t,_,_,_,t1) € TransducerGraph
        var candidateTransducerEdges =
            transducerGraph.getEdgesWithSourceAndTarget(transducerSource, transducerTarget);

        if(includeEpsilonEdges) {
            // keep only those that have matching u for some R s.t. (s,u,s1) € query and (t,u,v,w,t1) € trans
            candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming)) ||
                        transEdge.label.incoming.isEpsilonLabel()
            }
        }
        else {
            candidateTransducerEdges = candidateTransducerEdges.filter { transEdge ->
                candidateQueryTransitions.contains(QueryEdgeLabel(transEdge.label.incoming))
            }
        }

        if (candidateTransducerEdges.isEmpty()) {
            return null;
        }

        return candidateTransducerEdges
    }
}


data class InitialFinalStateWrapper(
    val sourceInitialState: Boolean,
    val sourceFinalState: Boolean,
    val targetInitialState: Boolean,
    val targetFinalState: Boolean
)
