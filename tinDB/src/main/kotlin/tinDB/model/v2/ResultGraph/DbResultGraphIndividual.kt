package tinDB.model.v2.ResultGraph

import tinLIB.model.v2.ResultGraph.ResultGraphIndividual
import tinLIB.model.v2.graph.Node

class DbResultGraphIndividual(
    val databaseNode: Node
): ResultGraphIndividual(databaseNode.identifier)