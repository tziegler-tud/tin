package ProductAutomatonSpecification;

import Algorithms.EdgeType;
import DataProvider.DataProvider;
import Database.DatabaseEdge;
import Database.DatabaseGraph;
import Database.DatabaseNode;
import Query.QueryEdge;
import Query.QueryGraph;
import Query.QueryNode;
import Transducer.TransducerEdge;
import Transducer.TransducerGraph;
import Transducer.TransducerNode;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.HashSet;


public class ProductAutomatonConstructor {

    public String choice;
    public DataProvider dataProvider;

    QueryGraph queryGraph;
    TransducerGraph transducerGraph;
    DatabaseGraph databaseGraph;
    EdgeType edgeType;

    public ProductAutomatonGraph productAutomatonGraph;


    /**
     * constructor.
     * We call it with a String for our choice of data.
     * The constructor then overwrites the queryGraph, transducerGraph and databaseGraph with the according data received from the dataProvider.
     *
     * @param queryGraph      the query graph
     * @param transducerGraph the transducer graph
     * @param databaseGraph   the database graph
     */


    public ProductAutomatonConstructor(QueryGraph queryGraph, TransducerGraph transducerGraph, DatabaseGraph databaseGraph) {
        this.queryGraph = queryGraph;
        this.transducerGraph = transducerGraph;
        this.databaseGraph = databaseGraph;
        productAutomatonGraph = new ProductAutomatonGraph();
    }


    /**
     * this function constructs the productAutomaton.
     * It takes the queryGraph, transducerGraph and databaseGraph and adds edges to the productAutomatonGraph if the following condition hold:
     * (a)
     * (b) the queryGraph has an edge whose label can be replaced with a transducedLabel at cost k AND the transducedLabel is represented in the databaseGraph.
     * <p>
     * <p>
     * NOTE:
     * we do not simulate runs through the graphs yet. We simply add possible edges.
     * Later we check whether a path is valid. (i.e. first element is an initial state and last element is a final state)
     * This is the productAutomaton and does not represent a final solution!
     * <br/> --- <br/>
     * we portion the whole procedure into 3 main parts for clarity reasons.
     * <br/> --- <br/>
     * part (I):
     * we loop over every queryNode.
     * we loop over every edge of this queryNode (giving us the source, target and label)
     * we take the label of this edge.
     * <br/> --- <br/>
     * part (II):
     * we take the label and run it through the transducer.
     * if we find any "transduced" label we save the corresponding edge in the HashSet "fittingTransducerEdges". (giving us its source, target and cost)
     * <br/> --- <br/>
     * part (III):
     * we check whether
     * <p>
     * (2) the "transduced" label is present in the databaseGraph.
     * TODO: here we check (label \in alphabet_pos) or (label \in alphabet_neg)
     * TODO: if (1) add positive edge (as we do at the moment)
     * TODO: else if (2) add neg edge (from target to source with label (label should be negative)
     * -> Yes?: we add the following edge to the productAutomaton:
     * (source) -[label/transducedLabel/cost]-> (target)
     * <br/> --- <br/>
     * <br/> --- <br/>
     * adding more functionality
     * <br/> --- <br/>
     * <br/> --- <br/>
     * part (IV) : epsilon edges
     * We expand the transducer to accept epsilon edges. "" (empty string) currently represents epsilon.
     * They can have two forms.
     * (1) (source) -[read: epsilon | write: String | cost: k]-> (target) ["incoming epsilon edges"]
     * This means we read the empty word and replace it with a String at cost k.
     * This can only happen if we are in a final queryState. We do not change the state of the query.
     * (basically we can only read the empty word at the end of our query. that means we concatenate the queryWord with a string. This cannot be done inside the queryString.
     * <br/> --- <br/>
     * (2) (source) -[read: String | write: epsilon | cost: k]-> (target) ["outgoing epsilon edges"]
     * This means we read a string and replace it with epsilon (empty String).
     * This can happen everywhere.
     * Doing this will forward the queryAutomaton despite not reading a String (since we replaced it with epsilon).
     */

    public void construct() {
        HashMap<String, ProductAutomatonNode> temporaryNodes = new HashMap<>();
        HashSet<TransducerEdge> fittingTransducerEdges = new HashSet<>();
        Pair<ProductAutomatonNode, ProductAutomatonNode> pairOfNodes;
        ProductAutomatonNode source;
        ProductAutomatonNode target;
        String testString = "";


        //part (I)
        for (QueryNode queryNode : queryGraph.nodes) {
            for (QueryEdge queryEdge : queryNode.edges) {
                String localQueryLabel = queryEdge.label;

                // part (II)
                // NOTE: here we also add edges of the form (IV | 2) "outgoing epsilon edges"
                fittingTransducerEdges.clear();
                for (TransducerNode transducerNode : transducerGraph.nodes) {
                    for (TransducerEdge transducerEdge : transducerNode.edges) {
                        if (localQueryLabel.equals(transducerEdge.incomingString)) {
                            fittingTransducerEdges.add(transducerEdge);
                        }

                        // add incoming epsilon edges where they can be applied
                        if (queryNode.isFinalState()) {
                            if (transducerEdge.incomingString.isBlank()) {
                                fittingTransducerEdges.add(transducerEdge);
                            }
                        }
                    }
                }

                // part (III)
                for (DatabaseNode databaseNode : databaseGraph.nodes) {
                    for (DatabaseEdge databaseEdge : databaseNode.edges) {
                        String localDatabaseLabel = databaseEdge.label;

                        // (2)
                        // for all transducer edges that were found in part (II):
                        //      check whether
                        for (TransducerEdge transducerEdge : fittingTransducerEdges) {

                            if (transducerEdge.incomingString.isBlank()) {
                                // type 1:  incoming epsilon edges.
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // epsilon incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.epsilonIncomingEpsilonOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", "", transducerEdge.cost);


                                } else if ((isNegated(transducerEdge.outgoingString)) && (localDatabaseLabel.equals(unNegateString(transducerEdge.outgoingString)))) {
                                    // epsilon incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.epsilonIncomingNegativeOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", transducerEdge.outgoingString, transducerEdge.cost);
                                } else if (!isNegated(transducerEdge.outgoingString) && (localDatabaseLabel.equals(transducerEdge.outgoingString))) {
                                    // epsilon incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.epsilonIncomingPositiveOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, "", transducerEdge.outgoingString, transducerEdge.cost);
                                }

                            } else if (!isNegated(transducerEdge.incomingString)) {
                                // type 2: incoming positive edges
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // positive incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.positiveIncomingEpsilonOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, "", transducerEdge.cost);
                                } else if ((!isNegated(transducerEdge.outgoingString)) && (localDatabaseLabel.equals(transducerEdge.outgoingString))) {
                                    // positive incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.positiveIncomingPositiveOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost);
                                } else if ((isNegated(transducerEdge.outgoingString)) && (localDatabaseLabel.equals(unNegateString(transducerEdge.outgoingString)))) {
                                    // positive incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.positiveIncomingNegativeOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost);
                                }

                            } else if (isNegated(transducerEdge.incomingString)) {
                                // type 3: incoming negative edges
                                if (transducerEdge.outgoingString.isBlank()) {
                                    // negative incoming, epsilon outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.negativeIncomingEpsilonOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, "", transducerEdge.cost);
                                } else if ((!isNegated(transducerEdge.outgoingString)) && (localDatabaseLabel.equals(transducerEdge.outgoingString))) {
                                    // negative incoming, positive outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.negativeIncomingPositiveOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost);
                                } else if ((isNegated(transducerEdge.outgoingString)) && (localDatabaseLabel.equals(unNegateString(transducerEdge.outgoingString)))) {
                                    // negative incoming, negative outgoing
                                    pairOfNodes = constructAutomatonNode(queryEdge, transducerEdge, databaseEdge, EdgeType.negativeIncomingNegativeOutgoing);
                                    source = pairOfNodes.getValue0();
                                    target = pairOfNodes.getValue1();
                                    source = getInstance(temporaryNodes, source); // duplicate check
                                    target = getInstance(temporaryNodes, target); // duplicate check
                                    productAutomatonGraph.addProductAutomatonEdge(source, target, transducerEdge.incomingString, transducerEdge.outgoingString, transducerEdge.cost);
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
    private Pair<ProductAutomatonNode, ProductAutomatonNode> constructAutomatonNode(QueryEdge queryEdge, TransducerEdge transducerEdge, DatabaseEdge databaseEdge, EdgeType edgeType) {

        boolean sourceInitialState;
        boolean sourceFinalState;
        boolean targetInitialState;
        boolean targetFinalState;
        Pair<ProductAutomatonNode, ProductAutomatonNode> resultPairOfNodes;

        ProductAutomatonNode sourceNode;
        ProductAutomatonNode targetNode;

        /**
         * last modified: 2021_03_16
         * TODO: this is a nice explanation, however it does not fit here. we just create automaton nodes here! (not the edges..)
         * Currently we support 2RPQs. We have 6 different edge types that can appear.
         * Below you can see when they arise (i) and how we handle them (ii).
         * <br/> --- <br/>
         * <br/> --- <br/>
         * (1) incoming epsilon.
         * (i) our query is in a final state and the transducer allows to read epsilon and replace it with some string.
         * (ii) query stays in the same state, transducer moves, database moves.
         * <br/> --- <br/>
         * (2) outgoing epsilon.
         * (i) can arise everywhere. We read some String and the transducer allows the deletion of said String at cost k.
         * (ii) query moves, transducer moves, database stays in the same state.
         * <br/> --- <br/>
         * (3) approximated.
         * (i) can arise everywhere. We read some String and the transducer then changes this String to some other String (incl. duplicates)
         * (ii) query moves, transducer moves, database moves.
         * <br/> --- <br/>
         *  *** Now for the inverse variants of these edge types
         * <br/> --- <br/>
         * (4) inverse incoming epsilon.
         * (i) same as (1) (i) - note that we read an inverse label (outgoing transducer).
         * (ii) query stays in the same state, transducer moves, database moves backwards
         * <br/> --- <br/>
         * (5) inverse outgoing epsilon.
         * (i) same as (2) (i) - note that we read an inverse label (incoming transducer).
         * (ii) query moves, transducer moves, database stays in the same state.
         * <br/> --- <br/>
         * (6) inverse approximated.
         * (i) same as (3) (i) - note that we read an inverse label (outgoing transducer).
         *
         */
        switch (edgeType) {
            // type 1
            case epsilonIncomingPositiveOutgoing:
                // epsilon incoming, positive outgoing
                // q pause, t move, db move

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.source.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.source.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState);

                break;
            case epsilonIncomingNegativeOutgoing:
                // epsilon incoming, negative outgoing
                // q pause, t move, db move backwards

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.source.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.source.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState);
                break;
            case epsilonIncomingEpsilonOutgoing:
                // epsilon incoming, epsilon outgoing
                // q pause, t move, db pause

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.source.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.source.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState);
                break;

            // type 2
            case positiveIncomingPositiveOutgoing:
                // positive incoming, positive outgoing
                // q move, t move, db move

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.target.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.target.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState);
                break;
            case positiveIncomingNegativeOutgoing:
                // positive incoming, negative outgoing
                // q move, t move, db move backwards

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.target.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.target.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState);
                break;
            case positiveIncomingEpsilonOutgoing:
                // positive incoming, epsilon outgoing
                // q move, t move, db pause

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.target.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.target.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState);
                break;

            // type 3
            case negativeIncomingPositiveOutgoing:
                // negative incoming, positive outgoing
                // q move, t move, db move

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.target.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.target.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.target, targetInitialState, targetFinalState);
                break;
            case negativeIncomingNegativeOutgoing:
                // negative incoming, negative outgoing
                // q move, t move, db move backwards

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.target.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.target.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.target, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState);
                break;
            case negativeIncomingEpsilonOutgoing:
                // negative incoming, epsilon outgoing
                // q move, t move, db pause

                sourceInitialState = (queryEdge.source.isInitialState() && transducerEdge.source.isInitialState());
                sourceFinalState = (queryEdge.source.isFinalState() && transducerEdge.source.isFinalState());

                targetInitialState = (queryEdge.target.isInitialState() && transducerEdge.target.isInitialState());
                targetFinalState = (queryEdge.target.isFinalState() && transducerEdge.target.isFinalState());

                sourceNode = new ProductAutomatonNode(queryEdge.source, transducerEdge.source, databaseEdge.source, sourceInitialState, sourceFinalState);
                targetNode = new ProductAutomatonNode(queryEdge.target, transducerEdge.target, databaseEdge.source, targetInitialState, targetFinalState);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + edgeType);
        }
        resultPairOfNodes = new Pair<>(sourceNode, targetNode);

        return resultPairOfNodes;

    }

    /**
     * this method checks for possible duplicates.
     *
     * @param nodeMap the map containing all the nodes.
     * @param node    the concrete node we want to test
     * @return node, part of the nodeMap. (either retrieved from there or newly created and added)
     */
    private ProductAutomatonNode getInstance(HashMap<String, ProductAutomatonNode> nodeMap, ProductAutomatonNode node) {

        if (nodeMap.containsKey(node.getIdentifier())) {
            node = nodeMap.get(node.getIdentifier());

        } else nodeMap.put(node.getIdentifier(), node);

        return node;
    }

    // returns if the string starts with a negation
    private boolean isNegated(String string) {
        return string.charAt(0) == '-';
    }

    // returns a negated string
    private String negateString(String string) {
        StringBuilder sb = new StringBuilder(string);

        if (string.charAt(0) != '-') {
            sb.insert(0, '-');
        }
        return sb.toString();
    }

    // returns an unnegated string
    private String unNegateString(String string) {
        String result;

        if (string.startsWith("-")) {
            result = string.substring(1);
        } else result = string;
        return result;
    }


}
