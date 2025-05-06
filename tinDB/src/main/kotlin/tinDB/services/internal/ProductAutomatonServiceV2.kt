package tinDB.services.internal

import tinDB.model.v2.DatabaseGraph.DatabaseEdge
import tinDB.model.v2.DatabaseGraph.DatabaseProperty
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v2.productAutomaton.*

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.EdgeLabelProperty
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.transducer.TransducerEdge

class ProductAutomatonServiceV2() {
    lateinit var dataProvider: RegularPathQueryDataProvider

    /**
     * constructs the product automaton accordingly to the Grahne & Thomo paper (2006)
     */
    fun constructProductAutomaton(regularPathQueryDataProvider: RegularPathQueryDataProvider): ProductAutomatonGraph {
        dataProvider = regularPathQueryDataProvider

        val temporaryNodes = HashMap<String, ProductAutomatonNode>()
        val fittingTransducerEdges = HashSet<TransducerEdge>()
        var pairOfNodes: Pair<ProductAutomatonNode, ProductAutomatonNode>
        var source: ProductAutomatonNode
        var target: ProductAutomatonNode
        val productAutomatonGraph = ProductAutomatonGraph()

        val incomingEpsilonEdges = HashSet<TransducerEdge>()
        val propertyTransducerEdges = HashSet<TransducerEdge>()

        val transducerGraph = regularPathQueryDataProvider.transducerGraph
        val queryGraph = regularPathQueryDataProvider.queryGraph
        val databaseGraph = regularPathQueryDataProvider.databaseGraph

        val resultGraph: ProductAutomatonGraph

        if (regularPathQueryDataProvider.queryGraph.nodes.isEmpty()) {

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
        for (queryEdge in queryGraph.edges) {
            val localQueryLabel = queryEdge.label

            // part (II)
            // NOTE: here we also add edges of the form (IV | 2) "outgoing epsilon edges"
            fittingTransducerEdges.clear()
            fittingTransducerEdges.addAll(incomingEpsilonEdges)
            propertyTransducerEdges.addAll(incomingEpsilonEdges)
            for (transducerEdge in transducerGraph.edges) {
                if (localQueryLabel.label.matches(transducerEdge.label.incoming)) {
                    fittingTransducerEdges.add(transducerEdge)
                } else {
                    // todo: this needs to be lifted outside of the forEach queryEdge loop.
                    //  this is because incoming epsilon edges must not require a queryEdge.
                    // add incoming epsilon edges where they can be applied
                    if (transducerEdge.source.isFinalState && transducerEdge.label.incoming.isEpsilonLabel()) {
                        fittingTransducerEdges.add(transducerEdge)
                    }
                }
            }

            // PA Graph construction Steps 1-9 + 14 + 15
            for (databaseEdge in databaseGraph.edges) {
                val localDatabaseLabel = databaseEdge.label
                val databaseSourceNode = databaseEdge.source
                val databaseTargetNode = databaseEdge.target

                var edgeType: ProductAutomatonEdgeType? = null
                //CAUTION: Below here belong ONLY cases 2,3,5,6,8,9 i.e. the ones that require a database edge!

                // (2)
                // for all transducer edges that were found in part (II):
                //      check whether
                for (transducerEdge in fittingTransducerEdges) {

                    // type 1:  incoming epsilon edges.
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

                    pairOfNodes = constructAutomatonNode(
                        queryEdge,
                        transducerEdge,
                        databaseEdge,
                        ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing
                    )
                    source = pairOfNodes.first
                    target = pairOfNodes.second

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
                    productAutomatonGraph.addEdge(edge)
                }

                // PA Graph construction 1,4,7,10-13,16
                val databaseLoop = DatabaseEdge(
                    source = databaseSourceNode,
                    target = databaseTargetNode,
                    label = replaceEmptyStringWithInternalEpsilon()
                )

                for (transducerEdge in fittingTransducerEdges) {
                    if (transducerEdge.label.incoming.isEpsilonLabel()) {
                        // type 1:  incoming epsilon edges.
                        if (transducerEdge.label.outgoing.isEpsilonLabel()) {
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
                            productAutomatonGraph.addEdge(edge)
                        } else
                            if (transducerEdge.label.outgoing.isConceptAssertion() && databaseSourceNode.hasProperty(
                                    DatabaseProperty(transducerEdge.label.outgoing.getLabel())
                                )
                            ) {
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
                    } else if (isPropertyAssertion(transducerEdge.incomingString)) {
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
                        } else if (isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(
                                Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString)
                            )
                        ) {
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
                    } else if ((!isNegated(transducerEdge.incomingString)) && !isPropertyAssertion(transducerEdge.incomingString)) {
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
                            if (isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(
                                    Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString)
                                )
                            ) {
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
                    } else if (isNegated(transducerEdge.incomingString) && !isPropertyAssertion(transducerEdge.incomingString)) {
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
                            if (isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(
                                    Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString)
                                )
                            ) {
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
                // q pause, t move, db pause (we do the property check ..)

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
                // this case is redundant, since the case PositiveIncomingPositiveOutgoing handles this already
                //  (it doesn't distinguish between positive incoming and property incoming)
                //  this block is never called

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
                // this case is redundant, since the case PositiveIncomingNegativeOutgoing handles this already
                //  (it doesn't distinguish between positive incoming and property incoming)
                //  this block is never called

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
                // this case is redundant, since the case PositiveIncomingPositiveOutgoing handles this already
                //  (it doesn't distinguish between positive incoming and property incoming)
                //  this block is never called

                // property incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState

                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
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
     * this method checks for possible duplicates.
     *
     * @param nodeMap the map containing all the nodes.
     * @param node    the concrete node we want to test
     * @return node, part of the nodeMap. (either retrieved from there or newly created and added)
     */
    private fun getInstance(
        nodeMap: HashMap<String, ProductAutomatonNode>,
        node: ProductAutomatonNode
    ): ProductAutomatonNode {
        var otherNode: ProductAutomatonNode = node
        if (nodeMap.containsKey(node.identifierString)) {
            otherNode = nodeMap[node.identifierString]!!
        } else nodeMap[node.identifierString] = node
        return otherNode
    }
}


data class InitialFinalStateWrapper(
    val sourceInitialState: Boolean,
    val sourceFinalState: Boolean,
    val targetInitialState: Boolean,
    val targetFinalState: Boolean
)
