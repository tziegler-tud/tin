package tin.model.productAutomaton

import dataProvider.DataProvider
import org.javatuples.Pair
import tin.model.database.DatabaseEdge
import tin.model.database.DatabaseGraph
import tin.model.query.QueryEdge
import tin.model.query.QueryGraph
import tin.model.transducer.TransducerEdge
import tin.model.transducer.TransducerGraph
import java.util.*

class ProductAutomatonConstructor(
        var queryGraph: QueryGraph,
        var transducerGraph: TransducerGraph,
        var databaseGraph: DatabaseGraph,
        var dataProvider: DataProvider,
        var productAutomatonGraph: ProductAutomatonGraph

) {


    /**
     * constructor.
     * We call it with a String for our choice of data.
     * The constructor then overwrites the queryGraph, transducerGraph and databaseGraph with the according data received from the dataProvider.
     *
     * @param queryGraph      the query graph
     * @param transducerGraph the transducer graph
     * @param databaseGraph   the database graph
     */
    init {
        productAutomatonGraph = ProductAutomatonGraph()
    }

    /**
     * this function constructs the productAutomaton.
     * It takes the queryGraph, transducerGraph and databaseGraph and adds edges to the productAutomatonGraph if the following condition hold:
     * (a)
     * (b) the queryGraph has an edge whose label can be replaced with a transducedLabel at cost k AND the transducedLabel is represented in the databaseGraph.
     *
     *
     *
     *
     * NOTE:
     * we do not simulate runs through the graphs yet. We simply add possible edges.
     * Later we check whether a path is valid. (i.e. first element is an initial state and last element is a final state)
     * This is the productAutomaton and does not represent a final solution!
     * <br></br> --- <br></br>
     * we portion the whole procedure into 3 main parts for clarity reasons.
     * <br></br> --- <br></br>
     * part (I):
     * we loop over every queryNode.
     * we loop over every edge of this queryNode (giving us the source, target and label)
     * we take the label of this edge.
     * <br></br> --- <br></br>
     * part (II):
     * we take the label and run it through the transducer.
     * if we find any "transduced" label we save the corresponding edge in the HashSet "fittingTransducerEdges". (giving us its source, target and cost)
     * <br></br> --- <br></br>
     * part (III):
     * we check whether
     *
     *
     * (2) the "transduced" label is present in the databaseGraph.
     * TODO: here we check (label \in alphabet_pos) or (label \in alphabet_neg)
     * TODO: if (1) add positive edge (as we do at the moment)
     * TODO: else if (2) add neg edge (from target to source with label (label should be negative)
     * -> Yes?: we add the following edge to the productAutomaton:
     * (source) -[label/transducedLabel/cost]-> (target)
     * <br></br> --- <br></br>
     * <br></br> --- <br></br>
     * adding more functionality
     * <br></br> --- <br></br>
     * <br></br> --- <br></br>
     * part (IV) : epsilon edges
     * We expand the transducer to accept epsilon edges. "" (empty string) currently represents epsilon.
     * They can have two forms.
     * (1) (source) -[read: epsilon | write: String | cost: k]-> (target) ["incoming epsilon edges"]
     * This means we read the empty word and replace it with a String at cost k.
     * This can only happen if we are in a final queryState. We do not change the state of the query.
     * (basically we can only read the empty word at the end of our query. that means we concatenate the queryWord with a string. This cannot be done inside the queryString.
     * <br></br> --- <br></br>
     * (2) (source) -[read: String | write: epsilon | cost: k]-> (target) ["outgoing epsilon edges"]
     * This means we read a string and replace it with epsilon (empty String).
     * This can happen everywhere.
     * Doing this will forward the queryAutomaton despite not reading a String (since we replaced it with epsilon).
     */
    fun construct() {
        val temporaryNodes = HashMap<String, ProductAutomatonNode>()
        val fittingTransducerEdges = HashSet<TransducerEdge>()
        var pairOfNodes: Pair<ProductAutomatonNode, ProductAutomatonNode>
        var source: ProductAutomatonNode
        var target: ProductAutomatonNode
        val testString = ""


        //part (I)
        for (queryNode in queryGraph.nodes!!) {
            for (queryEdge in queryNode.edges) {
                val localQueryLabel = queryEdge.label

                // part (II)
                // NOTE: here we also add edges of the form (IV | 2) "outgoing epsilon edges"
                fittingTransducerEdges.clear()
                for (transducerNode in transducerGraph.nodes) {
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
                for (databaseNode in databaseGraph.nodes!!) {
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
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.epsilonIncomingEpsilonOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", "", transducerEdge.cost)
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(transducerEdge.outgoingString)) {
                                    // epsilon incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.epsilonIncomingNegativeOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", transducerEdge.outgoingString, transducerEdge.cost)
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // epsilon incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.epsilonIncomingPositiveOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", transducerEdge.outgoingString, transducerEdge.cost)
                                }
                            } else if (!isNegated(transducerEdge.incomingString)) {
                                // type 2: incoming positive edges
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // positive incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.positiveIncomingEpsilonOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, "", transducerEdge.cost)
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // positive incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.positiveIncomingPositiveOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(transducerEdge.outgoingString)) {
                                    // positive incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.positiveIncomingNegativeOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                }
                            } else if (isNegated(transducerEdge.incomingString)) {
                                // type 3: incoming negative edges
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // negative incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.negativeIncomingEpsilonOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, "", transducerEdge.cost)
                                } else if (!isNegated(transducerEdge.outgoingString) && localDatabaseLabel == transducerEdge.outgoingString) {
                                    // negative incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.negativeIncomingPositiveOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
                                    source = getInstance(temporaryNodes, source) // duplicate check
                                    target = getInstance(temporaryNodes, target) // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost)
                                } else if (isNegated(transducerEdge.outgoingString) && localDatabaseLabel == unNegateString(transducerEdge.outgoingString)) {
                                    // negative incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.negativeIncomingNegativeOutgoing)
                                    source = pairOfNodes.value0
                                    target = pairOfNodes.value1
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
    }
    //}
    /**
     * helper function that builds us the ProductAutomatonNodes needed to create new edges.
     *
     * @param queryEdge      the respective queryEdge
     * @param transducerEdge the respective transducerEdge
     * @param databaseEdge   the respective databaseEdge
     * @param edgeType       the edgeType that needs to be created.
     * @return a Pair of ProductAutomatonNodes, containing the source node (value_0) and the target node (value_1)
     */
    private fun constructAutomatonNode(queryEdge: QueryEdge, transducerEdge: TransducerEdge, databaseEdge: DatabaseEdge, edgeType: EdgeType): Pair<ProductAutomatonNode, ProductAutomatonNode> {
        val sourceInitialState: Boolean
        val sourceFinalState: Boolean
        val targetInitialState: Boolean
        val targetFinalState: Boolean
        val resultPairOfNodes: Pair<ProductAutomatonNode, ProductAutomatonNode>
        val sourceNode: ProductAutomatonNode
        val targetNode: ProductAutomatonNode
        when (edgeType) {
            EdgeType.epsilonIncomingPositiveOutgoing -> {
                // epsilon incoming, positive outgoing
                // q pause, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState)
            }

            EdgeType.epsilonIncomingNegativeOutgoing -> {
                // epsilon incoming, negative outgoing
                // q pause, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            EdgeType.epsilonIncomingEpsilonOutgoing -> {
                // epsilon incoming, epsilon outgoing
                // q pause, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.source.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.source.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            EdgeType.positiveIncomingPositiveOutgoing -> {
                // positive incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState)
            }

            EdgeType.positiveIncomingNegativeOutgoing -> {
                // positive incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            EdgeType.positiveIncomingEpsilonOutgoing -> {
                // positive incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            EdgeType.negativeIncomingPositiveOutgoing -> {
                // negative incoming, positive outgoing
                // q move, t move, db move
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState)
            }

            EdgeType.negativeIncomingNegativeOutgoing -> {
                // negative incoming, negative outgoing
                // q move, t move, db move backwards
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            EdgeType.negativeIncomingEpsilonOutgoing -> {
                // negative incoming, epsilon outgoing
                // q move, t move, db pause
                sourceInitialState = queryEdge.source.isInitialState && transducerEdge.source.isInitialState
                sourceFinalState = queryEdge.source.isFinalState && transducerEdge.source.isFinalState
                targetInitialState = queryEdge.target.isInitialState && transducerEdge.target.isInitialState
                targetFinalState = queryEdge.target.isFinalState && transducerEdge.target.isFinalState
                sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState)
                targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState)
            }

            else -> throw IllegalStateException("Unexpected value: $edgeType")
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
    private fun getInstance(nodeMap: HashMap<String, ProductAutomatonNode>, node: ProductAutomatonNode): ProductAutomatonNode{
        var node = node
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

    // returns an unnegated string
    private fun unNegateString(string: String): String {
        val result: String = if (string.startsWith("-")) {
            string.substring(1)
        } else string
        return result
    }
}
