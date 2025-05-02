package tinDB.services.internal

import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v2.DatabaseGraph.DatabaseEdge
import tinDB.model.v1.productAutomaton.ProductAutomatonEdgeType
import tinDB.model.v1.productAutomaton.ProductAutomatonGraph
import tinDB.model.v1.productAutomaton.ProductAutomatonNode
import tinDB.services.ontology.ResultGraph.DbResultGraphBuilder

import tinLIB.model.v2.alphabet.Alphabet
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

        val resultGraph: DbResultGraph

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
                }
                else {
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

                    //CAUTION: Below here belong ONLY cases 2,3,5,6,8,9 i.e. the ones that require a database edge!

                    // (2)
                    // for all transducer edges that were found in part (II):
                    //      check whether
                    for (transducerEdge in fittingTransducerEdges) {
                        if (transducerEdge.label.incoming.isEpsilonLabel()) {
                            // type 1:  incoming epsilon edges.
                            if (transducerEdge.label.outgoing.isInverse() &&
                                !localDatabaseLabel.label.isInverse() &&
                                //compare raw string labels
                                localDatabaseLabel.label.getLabel() == transducerEdge.label.outgoing.getLabel()
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
                val databaseLoop = tinDB.model.v1.database.DatabaseEdge(
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
                            if (isPropertyAssertion(transducerEdge.outgoingString) && databaseNode.hasProperty(
                                    Alphabet.conceptNameFromAssertion(transducerEdge.outgoingString)
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


        return resultPairOfNodes
    }

