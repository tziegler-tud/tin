package tin.services.internal

import org.springframework.stereotype.Service
import tin.model.alphabet.Alphabet
import tin.model.dataProvider.DataProvider
import tin.model.database.DatabaseEdge
import tin.model.productAutomaton.ProductAutomatonEdgeType
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.productAutomaton.ProductAutomatonNode
import tin.model.query.QueryEdge
import tin.model.transducer.TransducerEdge

@Service
class ProductAutomatonService() {

    /**
     * constructs the product automaton accordingly to the Grahne & Thomo paper (2006)
     */
    fun constructProductAutomaton(dataProvider: DataProvider): ProductAutomatonGraph {

        val temporaryNodes = HashMap<String, ProductAutomatonNode>()
        val fittingTransducerEdges = HashSet<TransducerEdge>()
        var pairOfNodes: Pair<ProductAutomatonNode, ProductAutomatonNode>
        var source: ProductAutomatonNode
        var target: ProductAutomatonNode
        val productAutomatonGraph = ProductAutomatonGraph()

        val incomingEpsilonEdges = HashSet<TransducerEdge>()
        val propertyTransducerEdges = HashSet<TransducerEdge>()
        if (dataProvider.queryGraph.nodes.isEmpty()) {

            // find all incoming epsilon edges
            // bandaid fix to insert incoming epsilon edges to fitting transducer edge every iteration.

            dataProvider.transducerGraph.nodes.forEach { transducerNode ->
                transducerNode.edges?.forEach { transducerEdge ->
                    if (isEpsilonString(transducerEdge.incomingString)) {
                        incomingEpsilonEdges.add(transducerEdge)
                    }
                }
            }
        }

        //part (I)
        for (queryNode in dataProvider.queryGraph.nodes) {
            for (queryEdge in queryNode.edges) {
                val localQueryLabel = queryEdge.label

                // part (II)
                // NOTE: here we also add edges of the form (IV | 2) "outgoing epsilon edges"
                fittingTransducerEdges.clear()
                fittingTransducerEdges.addAll(incomingEpsilonEdges)
                propertyTransducerEdges.addAll(incomingEpsilonEdges)
                for (transducerNode in dataProvider.transducerGraph.nodes) {
                    for (transducerEdge in transducerNode.edges!!) {
                        if (localQueryLabel == transducerEdge.incomingString) {
                            fittingTransducerEdges.add(transducerEdge)
                        }

                        // todo: this needs to be lifted outside of the forEach queryEdge loop.
                        //  this is because incoming epsilon edges must not require a queryEdge.
                        // add incoming epsilon edges where they can be applied
                        if (queryNode.isFinalState) {
                            if (isEpsilonString(transducerEdge.incomingString)) {
                                fittingTransducerEdges.add(transducerEdge)
                            }
                        }
                    }
                }

                // part (III)
                for (databaseNode in dataProvider.databaseGraph.nodes) {
                    // PA Graph construction Steps 1-9 + 14 + 15
                    for (databaseEdge in databaseNode.edges) {
                        val localDatabaseLabel = databaseEdge.label

                        //CAUTION: Below here belong ONLY cases 2,3,5,6,8,9 i.e. the ones that require a database edge!

                        // (2)
                        // for all transducer edges that were found in part (II):
                        //      check whether
                        for (transducerEdge in fittingTransducerEdges) {
                            if (isEpsilonString(transducerEdge.incomingString)) {
                                // type 1:  incoming epsilon edges.
                                if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(
                                        transducerEdge.outgoingString
                                    )
                                ) {
                                    // epsilon incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseEdge,
                                        ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing
                                    )
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = replaceEmptyStringWithInternalEpsilon(),
                                        transducerEdge.outgoingString,
                                        transducerEdge.cost
                                    )
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // epsilon incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseEdge,
                                        ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing
                                    )
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = replaceEmptyStringWithInternalEpsilon(),
                                        transducerEdge.outgoingString,
                                        transducerEdge.cost
                                    )
                                }
                            } else if (!isNegated(transducerEdge.incomingString)) {
                                if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // positive incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseEdge,
                                        ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing
                                    )
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        transducerEdge.incomingString,
                                        transducerEdge.outgoingString,
                                        transducerEdge.cost
                                    )
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(
                                        transducerEdge.outgoingString
                                    )
                                ) {
                                    // positive incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseEdge,
                                        ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing
                                    )
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        transducerEdge.incomingString,
                                        transducerEdge.outgoingString,
                                        transducerEdge.cost
                                    )
                                }
                            } else if (isNegated(transducerEdge.incomingString)) {
                                // type 3: incoming negative edges
                                if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // negative incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseEdge,
                                        ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing
                                    )
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        transducerEdge.incomingString,
                                        transducerEdge.outgoingString,
                                        transducerEdge.cost
                                    )
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(
                                        transducerEdge.outgoingString
                                    )
                                ) {
                                    // negative incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseEdge,
                                        ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing
                                    )
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        transducerEdge.incomingString,
                                        transducerEdge.outgoingString,
                                        transducerEdge.cost
                                    )
                                }
                            }
                        }
                    }
                    // PA Graph construction 1,4,7,10-13,16
                    val databaseLoop = DatabaseEdge(
                            source = databaseNode,
                            target = databaseNode,
                            label = replaceEmptyStringWithInternalEpsilon()
                    )
                    for (transducerEdge in fittingTransducerEdges) {
                        if (isEpsilonString(transducerEdge.incomingString)) {
                            // type 1:  incoming epsilon edges.
                            if (isEpsilonString(transducerEdge.outgoingString)) {
                                // CASE 1
                                // epsilon incoming, epsilon outgoing
                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = replaceEmptyStringWithInternalEpsilon(),
                                        outgoing = replaceEmptyStringWithInternalEpsilon(),
                                        transducerEdge.cost
                                )
                            } else
                            if (isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString))) {
                                // epsilon incoming, outgoing property assertions
                                // PA construction step 10
                                //create a virtual edge on-the-fly to use in function. Label can be ignored here.

                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.EpsilonIncomingPropertyOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = replaceEmptyStringWithInternalEpsilon(),
                                        outgoing = transducerEdge.outgoingString,
                                        transducerEdge.cost
                                )
                            }
                        }
                        else if (isPropertyAssertion(transducerEdge.incomingString)) {
                            // type 1:  incoming property edge
                            if (isEpsilonString(transducerEdge.outgoingString)) {
                                // property incoming, epsilon outgoing
                                // PA construction step 13
                                //databaseNode can be ignored here!
                                //create a virtual edge on-the-fly to use in function

                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.PropertyIncomingEpsilonOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = transducerEdge.incomingString,
                                        outgoing = replaceEmptyStringWithInternalEpsilon(),
                                        transducerEdge.cost
                                )
                            }
                            else if (isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString))) {
                                // property incoming, property outgoing
                                // PA construction step 16
                                //create a virtual edge on-the-fly to use in function

                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.PropertyIncomingPropertyOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = transducerEdge.incomingString,
                                        outgoing = transducerEdge.outgoingString,
                                        transducerEdge.cost
                                )
                            }
                        }
                        else if ((!isNegated(transducerEdge.incomingString)) && !isPropertyAssertion(transducerEdge.incomingString)) {
                            //positive role incoming
                            if (isEpsilonString(transducerEdge.outgoingString)) {
                                // CASE 4: positive incoming, epsilon outgoing
                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        transducerEdge.incomingString,
                                        outgoing = replaceEmptyStringWithInternalEpsilon(),
                                        transducerEdge.cost
                                )
                            } else
                            if(isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString))) {
                                // positive incoming, Property outgoing
                                // PA construction step 11

                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.PositiveIncomingPropertyOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = transducerEdge.incomingString,
                                        outgoing = transducerEdge.outgoingString,
                                        transducerEdge.cost
                                )

                            }
                        }
                        else if (isNegated(transducerEdge.incomingString) && !isPropertyAssertion(transducerEdge.incomingString)) {
                            //negative role incoming
                            if (isEpsilonString(transducerEdge.outgoingString)) {
                                // CASE 7
                                // negative incoming, epsilon outgoing
                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        transducerEdge.incomingString,
                                        outgoing = replaceEmptyStringWithInternalEpsilon(),
                                        transducerEdge.cost
                                )
                            } else
                            if(isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString))) {
                                // negative incoming, Property outgoing
                                // PA construction step 12
                                pairOfNodes = constructAutomatonNode(
                                        queryEdge,
                                        transducerEdge,
                                        databaseLoop,
                                        ProductAutomatonEdgeType.NegativeIncomingPropertyOutgoing
                                )
                                source = pairOfNodes.first
                                target = pairOfNodes.second
                                source = getInstance(temporaryNodes, source) // duplicate check
                                target = getInstance(temporaryNodes, target) // duplicate check
                                productAutomatonGraph.addProductAutomatonEdge(
                                        source,
                                        target,
                                        incoming = transducerEdge.incomingString,
                                        outgoing = transducerEdge.outgoingString,
                                        transducerEdge.cost
                                )
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
        val sourceInitialState: Boolean
        val sourceFinalState: Boolean
        val targetInitialState: Boolean
        val targetFinalState: Boolean
        val resultPairOfNodes: Pair<ProductAutomatonNode, ProductAutomatonNode>
        val sourceNode: ProductAutomatonNode
        val targetNode: ProductAutomatonNode
        when (edgeType) {
            ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing -> {
                // epsilon incoming, positive outgoing
                // q pause, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing -> {
                // epsilon incoming, epsilon outgoing
                // q pause, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                // q pause, t move, db pause (we do the property check ..)

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
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
                // q move, t move, db pause (we do the property check ..)

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                // q move, t move, db pause (we do the property check ..)

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                // this case is redundant, since the case PositiveIncomingPositiveOutgoing handles this already
                //  (it doesn't distinguish between positive incoming and property incoming)
                //  this block is never called

                // property incoming, positive outgoing
                // q move, t move, db move

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
                // this case is redundant, since the case PositiveIncomingNegativeOutgoing handles this already
                //  (it doesn't distinguish between positive incoming and property incoming)
                //  this block is never called

                // property incoming, negative outgoing
                // q move, t move, db move backwards

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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

                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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


    /**
     * this method checks for possible duplicates.
     *
     * @param nodeMap the map containing all the nodes.
     * @param node    the concrete node we want to test
     * @return node, part of the nodeMap. (either retrieved from there or newly created and added)
     */
    private fun getInstance(nodeMap: HashMap<String, ProductAutomatonNode>, node: ProductAutomatonNode): ProductAutomatonNode {
        var otherNode: ProductAutomatonNode = node
        if (nodeMap.containsKey(node.identifierString)) {
            otherNode = nodeMap[node.identifierString]!!
        } else nodeMap[node.identifierString] = node
        return otherNode
    }

    // returns if the string starts with a negation
    private fun isNegated(string: String): Boolean {
        return string[0] == '-'
    }

    // returns a negated string
    private fun negateString(string: String): String {
        val sb = StringBuilder(string)
        if (string[0] != '-') {
            sb.insert(0, '-')
        }
        return sb.toString()
    }

    // returns a non negated string
    private fun unNegateString(string: String): String {
        val result: String = if (string.startsWith("-")) {
            string.substring(1)
        } else string
        return result
    }

    private fun isEpsilonString(string: String): Boolean {
        val listOfEpsilonEquivalents = arrayListOf("ε", "epsilon")
        return listOfEpsilonEquivalents.contains(string)
    }

    private fun isPropertyAssertion(string: String): Boolean {
        return Alphabet.isConceptAssertion(string);
    }

    /**
     * todo
     *  consider making the internal epsilon identifier a property the user can change in the frontend.
     *  (duplicate with TransducerReaderService)
     */
    private fun replaceEmptyStringWithInternalEpsilon(): String {
        return "epsilon"
    }
}