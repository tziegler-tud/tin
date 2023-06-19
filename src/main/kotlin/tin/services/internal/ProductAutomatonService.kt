package tin.services.internal

import tin.model.DataProvider
import tin.model.database.DatabaseEdge
import tin.model.productAutomaton.ProductAutomatonEdgeType
import tin.model.productAutomaton.ProductAutomatonGraph
import tin.model.productAutomaton.ProductAutomatonNode
import tin.model.query.QueryEdge
import tin.model.transducer.TransducerEdge


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


        //part (I)
        for (queryNode in dataProvider.queryGraph.nodes) {
            for (queryEdge in queryNode.edges) {
                val localQueryLabel = queryEdge.label

                // part (II)
                // NOTE: here we also add edges of the form (IV | 2) "outgoing epsilon edges"
                fittingTransducerEdges.clear()
                for (transducerNode in dataProvider.transducerGraph.nodes) {
                    for (transducerEdge in transducerNode.edges!!) {
                        if (localQueryLabel == transducerEdge.incomingString) {
                            fittingTransducerEdges.add(transducerEdge)
                        }

                        // add incoming epsilon edges where they can be applied
                        if (queryNode.isFinalState) {
                            if (transducerEdge.incomingString.isBlank()) {
                                fittingTransducerEdges.add(transducerEdge)
                            }
                        }
                    }
                }

                // part (III)
                for (databaseNode in dataProvider.databaseGraph.nodes) {
                    for (databaseEdge in databaseNode.edges) {
                        val localDatabaseLabel = databaseEdge.label

                        // (2)
                        // for all transducer edges that were found in part (II):
                        //      check whether
                        for (transducerEdge in fittingTransducerEdges) {
                            if (transducerEdge.incomingString.isBlank()) {
                                // type 1:  incoming epsilon edges.
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // epsilon incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.EpsilonIncomingEpsilonOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", "", transducerEdge.cost)
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(transducerEdge.outgoingString)) {
                                    // epsilon incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.EpsilonIncomingNegativeOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", transducerEdge.outgoingString, transducerEdge.cost)
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // epsilon incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.EpsilonIncomingPositiveOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", transducerEdge.outgoingString, transducerEdge.cost)
                                }
                            } else if (!isNegated(transducerEdge.incomingString)) {
                                // type 2: incoming positive edges
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // positive incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, "", transducerEdge.cost)
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // positive incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(transducerEdge.outgoingString)) {
                                    // positive incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                }
                            } else if (isNegated(transducerEdge.incomingString)) {
                                // type 3: incoming negative edges
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // negative incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, "", transducerEdge.cost)
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // negative incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(transducerEdge.outgoingString)) {
                                    // negative incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing)
                                    source = pairOfNodes.first
                                    target = pairOfNodes.second
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                }
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
    private fun constructAutomatonNode(queryEdge: QueryEdge, transducerEdge: TransducerEdge, databaseEdge: DatabaseEdge, edgeType: ProductAutomatonEdgeType): Pair<ProductAutomatonNode, ProductAutomatonNode> {
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
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState)
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
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.PositiveIncomingPositiveOutgoing -> {
                // positive incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.PositiveIncomingNegativeOutgoing -> {
                // positive incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.PositiveIncomingEpsilonOutgoing -> {
                // positive incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.NegativeIncomingPositiveOutgoing -> {
                // negative incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.NegativeIncomingNegativeOutgoing -> {
                // negative incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            ProductAutomatonEdgeType.NegativeIncomingEpsilonOutgoing -> {
                // negative incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
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
        var node: ProductAutomatonNode = node
        if (nodeMap.containsKey(node.identifierString)) {
            node = nodeMap[node.identifierString]!!
        } else nodeMap[node.identifierString] = node
        return node
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
}