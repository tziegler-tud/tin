package tinDB.services.ontology.ResultGraph

import tinDB.model.v2.DatabaseGraph.DatabaseEdgeLabel
import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinDB.model.v2.ResultGraph.DbResultEdge
import tinDB.model.v2.ResultGraph.DbResultEdgeSet
import tinDB.model.v2.ResultGraph.DbResultGraph
import tinDB.model.v2.ResultGraph.DbResultNode
import tinDB.services.ResultGraph.AbstractDbResultGraphBuilder
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph

import tinLIB.model.v2.graph.Node
import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.services.ontology.ResultGraph.AbstractResultGraphBuilder

class DbResultGraphBuilder(
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val databaseGraph: DatabaseGraph

    ) : AbstractDbResultGraphBuilder(queryGraph, transducerGraph, databaseGraph) {

    fun constructResultGraph() : DbResultGraph {

        val incomingEpsilonEdges = HashSet<TransducerEdge>()
        val propertyTransducerEdges = HashSet<TransducerEdge>()
        val fittingTransducerEdges = HashSet<TransducerEdge>()


        var resultGraph = DbResultGraph()

        if(queryGraph.nodes.isEmpty()) {
            // find all incoming epsilon edges
            // bandaid fix to insert incoming epsilon edges to fitting transducer edge every iteration.
            transducerGraph.edges.forEach { transducerEdge ->
                val incomingLabel = transducerEdge.label.incoming
                if (incomingLabel.isEpsilonLabel()) {
                    incomingEpsilonEdges.add(transducerEdge)
                }
            }
        }

        transducerGraph.edges.forEach { transducerEdge ->
            val incomingLabel = transducerEdge.label.incoming
            val outgoingLabel = transducerEdge.label.outgoing

            //Epsilon-Labeled rules

            when {
                incomingLabel.isEpsilonLabel() -> {
                    //epsilon-labeled rules (equation 3.9-3.12)

                }
                incomingLabel.isConceptAssertion() -> {
                    //concept-name labeled rules (equation 3.13-3.16)

                }
                incomingLabel.isRole()-> {
                    //role-name labeled rules (equations 3.1-3.8)

                    //these require a queryEdge with the same label (positive or negative)
                    val candidateQueryEdges = queryGraph.getEdgesWithLabel(QueryEdgeLabel(incomingLabel))


                    /**
                     * at this point, we have transEdge = (t,u,s,w,t') with:
                     * u is a (positive or negative) role name
                     * s is either: a (positive or negative) role name, concept assertion, or epsilon
                     */

                    when {
                        outgoingLabel.isEpsilonLabel() -> {
                            //equations 3.4 / 3.8

                        }
                        outgoingLabel.isConceptAssertion() -> {
                            //equations 3.3 / 3.7

                        }
                        outgoingLabel.isRole() -> {
                            //equations 3.1 / 3.2 / 3.5 / 3.6
                            val positiveLabelString = incomingLabel.getLabel() //contains the raw un-negated label text
                            //retrieve edges (d,s,d')
                            val canidateDbEdges = databaseGraph.getEdgesWithLabel(DatabaseEdgeLabel(positiveLabelString))

                            //add nodes to resultGraph
                            candidateQueryEdges.forEach { candidateQueryEdge ->
                                canidateDbEdges.forEach { canidateDbEdge ->
                                    val querySource = candidateQueryEdge.source
                                    val queryTarget = candidateQueryEdge.target
                                    val dbSource = canidateDbEdge.source
                                    val dbTarget = canidateDbEdge.target

                                    val resultSourceNode = DbResultNode(
                                        querySource, transducerEdge.source, dbSource
                                    )
                                    val resultTargetNode = DbResultNode(
                                        queryTarget, transducerEdge.target, dbTarget
                                    )
                                    val resultEdge = DbResultEdge(
                                        resultSourceNode,
                                        resultTargetNode,
                                        transducerEdge.label.cost
                                    )
                                }
                            }
                        }
                    }


                }
                else -> {
                    //Error
                }
            }


        }


        //type 3a: incoming positive role, outgoing positive role
        //type 3b: incoming positive role, outgoing negative role
        //type 3c: incoming positive role, outgoing epsilon
        //type 3d: incoming positive role, outgoing assertion

        //type 4: incoming Assertion
        //type 4a: incoming assertions, outgoing positive role
        //type 4b: incoming assertions, outgoing negative role
        //type 4c: incoming assertions, outgoing epsilon
        //type 4d: incoming assertions, outgoing assertion

        return resultGraph
    }

}