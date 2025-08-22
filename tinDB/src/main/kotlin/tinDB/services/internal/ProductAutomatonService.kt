package tinDB.services.internal

import tinDB.model.v2.DatabaseGraph.DatabaseEdge
import tinDB.model.v2.DatabaseGraph.DatabaseEdgeLabel
import tinDB.model.v2.DatabaseGraph.DatabaseNode
import tinDB.model.v2.DatabaseGraph.DatabaseProperty
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.model.v2.productAutomaton.*
import tinLIB.model.v2.graph.EdgeLabelProperty

import tinLIB.model.v2.query.QueryEdge
import tinLIB.model.v2.query.QueryEdgeLabel

class ProductAutomatonService(
    regularPathQueryDataProvider: RegularPathQueryDataProvider
) {
    private var productAutomatonGraph = ProductAutomatonGraph()

    private val transducerGraph = regularPathQueryDataProvider.transducerGraph
    private val queryGraph = regularPathQueryDataProvider.queryGraph
    private val databaseGraph = regularPathQueryDataProvider.databaseGraph

    /**
     * constructs the product automaton accordingly to the Grahne & Thomo paper (2006)
     * reworked implementation, needs testing (22.08.2025)
     */
    fun constructProductAutomaton(): ProductAutomatonGraph {
        val paGraph = ProductAutomatonGraph()

        for(transducerEdge in transducerGraph.edges) {
            val incomingLabel = transducerEdge.label.incoming
            val outgoingLabel = transducerEdge.label.outgoing
            val cost = transducerEdge.label.cost

            val productAutomatonEdgeLabel = ProductAutomatonEdgeLabel(incomingLabel, outgoingLabel, cost)


            /**
             * PA construction cases 1-8 + 13-16: incoming role (positive or negative) or concept assertion
             */
            if (incomingLabel.isRole() || incomingLabel.isConceptAssertion()) {
                //get matching query edges
                val fittingQueryEdges = getFittingQueryEdges(incomingLabel)

                /**
                 * PA construction steps 1,2,5,6,13,14
                 */
                if(outgoingLabel.isRole()){
                    //get matching DB edges
                    val fittingDbEdges = getFittingDatabaseEdges(outgoingLabel)

                    for(dbEdge in fittingDbEdges) {
                        //construct nodes and edge
                        val dbSource: DatabaseNode
                        val dbTarget: DatabaseNode

                        if(outgoingLabel.isInverse()){
                            dbSource = dbEdge.target
                            dbTarget = dbEdge.source
                        }
                        else {
                            dbSource = dbEdge.source
                            dbTarget = dbEdge.target
                        }

                        for(queryEdge in fittingQueryEdges) {
                            val sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, dbSource)
                            val targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, dbTarget)
                            val productAutomatonEdge = ProductAutomatonEdge(sourceNode, targetNode, productAutomatonEdgeLabel)
                            paGraph.addEdge(productAutomatonEdge)
                        }
                    }
                }

                /**
                 * PA construction steps 3,7
                 */
                if(outgoingLabel.isConceptAssertion()) {
                    val dbNodes = databaseGraph.getNodesWithProperty(DatabaseProperty(outgoingLabel.getLabel()))
                    for(queryEdge in fittingQueryEdges) {
                        for(dbNode in dbNodes) {
                            val sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, dbNode)
                            val targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, dbNode)
                            val productAutomatonEdge = ProductAutomatonEdge(sourceNode, targetNode, productAutomatonEdgeLabel)
                            paGraph.addEdge(productAutomatonEdge)
                        }
                    }
                }

                /**
                 * PA construction steps 4,8
                 */
                if(outgoingLabel.isEpsilonLabel()) {
                    for(queryEdge in fittingQueryEdges) {
                        for(dbNode in databaseGraph.nodes) {
                            val sourceNode = ProductAutomatonNode(queryEdge.source, transducerEdge.source, dbNode)
                            val targetNode = ProductAutomatonNode(queryEdge.target, transducerEdge.target, dbNode)
                            val productAutomatonEdge = ProductAutomatonEdge(sourceNode, targetNode, productAutomatonEdgeLabel)
                            paGraph.addEdge(productAutomatonEdge)
                        }
                    }
                }
            }
            /**
             * PA construction cases 9-12: incoming epsilon label
             */
            if (incomingLabel.isEpsilonLabel()) {
                /**
                 * PA construction steps 9,10
                 */
                if(outgoingLabel.isRole())  {
                    val fittingDbEdges = getFittingDatabaseEdges(outgoingLabel)
                    for(dbEdge in fittingDbEdges) {
                        //construct nodes and edge
                        val dbSource: DatabaseNode
                        val dbTarget: DatabaseNode

                        if(outgoingLabel.isInverse()){
                            dbSource = dbEdge.target
                            dbTarget = dbEdge.source
                        }
                        else {
                            dbSource = dbEdge.source
                            dbTarget = dbEdge.target
                        }

                        for(queryNode in queryGraph.nodes) {
                            val sourceNode = ProductAutomatonNode(queryNode, transducerEdge.source, dbSource)
                            val targetNode = ProductAutomatonNode(queryNode, transducerEdge.target, dbTarget)
                            val productAutomatonEdge = ProductAutomatonEdge(sourceNode, targetNode, productAutomatonEdgeLabel)
                            paGraph.addEdge(productAutomatonEdge)
                        }
                    }

                }

                /**
                 * PA construction step 11
                 */
                if(outgoingLabel.isConceptAssertion()){
                    val dbNodes = databaseGraph.getNodesWithProperty(DatabaseProperty(outgoingLabel.getLabel()))
                    for(dbNode in dbNodes) {
                        for(queryNode in queryGraph.nodes) {
                            val sourceNode = ProductAutomatonNode(queryNode, transducerEdge.source, dbNode)
                            val targetNode = ProductAutomatonNode(queryNode, transducerEdge.target, dbNode)
                            val productAutomatonEdge = ProductAutomatonEdge(sourceNode, targetNode, productAutomatonEdgeLabel)
                            paGraph.addEdge(productAutomatonEdge)
                        }

                    }
                }

                /**
                 * PA construction step 12
                 */
                if(outgoingLabel.isEpsilonLabel()) {
                    for(queryNode in queryGraph.nodes) {
                        for(dbNode in databaseGraph.nodes) {
                            val sourceNode = ProductAutomatonNode(queryNode, transducerEdge.source, dbNode)
                            val targetNode = ProductAutomatonNode(queryNode, transducerEdge.target, dbNode)
                            val productAutomatonEdge = ProductAutomatonEdge(sourceNode, targetNode, productAutomatonEdgeLabel)
                            paGraph.addEdge(productAutomatonEdge)
                        }
                    }
                }
            }
        }
        return productAutomatonGraph
    }


    private fun getFittingQueryEdges(
        label: EdgeLabelProperty
    ): List<QueryEdge> {
        return queryGraph.getEdgesWithLabel(QueryEdgeLabel(label))
    }

    /**
     * extracts database edges (d1,s,d2). If an inverse label (e.g. s-) is given, the non-inverse label (s) is used instead.
     * If an epsilon-Transition or Concept assertion is given, returns an empty set.
     */
    private fun getFittingDatabaseEdges(
        label: EdgeLabelProperty
    ): List<DatabaseEdge> {
        if(label.isConceptAssertion() || label.isEpsilonLabel() ) {
            val emptyList: List<DatabaseEdge> = listOf()
            return emptyList
        }
        val dbLabel: DatabaseEdgeLabel;
        if(label.isInverse()) {
            dbLabel = DatabaseEdgeLabel(label.getInverseAsNewProperty())
        }
        else dbLabel = DatabaseEdgeLabel(label)
        return databaseGraph.getEdgesWithLabel(dbLabel)
    }
}
