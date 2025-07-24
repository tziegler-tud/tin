package tinDB.services.ontology.ResultGraph

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinDB.model.v2.ResultGraph.DbResultGraph
import tinDB.model.v2.dataProvider.RegularPathQueryDataProvider
import tinDB.services.ResultGraph.AbstractDbResultGraphBuilder
import tinDB.services.internal.ProductAutomatonServiceV2
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph


class DbResultGraphBuilder(
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val databaseGraph: DatabaseGraph,

    ) : AbstractDbResultGraphBuilder(queryGraph, transducerGraph, databaseGraph) {

        fun constructResultGraph() : DbResultGraph{
            val dataProvider: RegularPathQueryDataProvider = RegularPathQueryDataProvider(
                queryGraph,
                transducerGraph,
                databaseGraph,
                null,
                null
            )

            val productAutomatonService = ProductAutomatonServiceV2(dataProvider)
            val productAutomatonGraph = productAutomatonService.constructProductAutomaton();

            val resultGraph: DbResultGraph = DbResultGraph.fromProductAutomaton(productAutomatonGraph);
            return resultGraph;
        }



}