package tinDB.model.v2.DatabaseGraph

import tinDB.model.v2.ResultGraph.DbResultGraphIndividual
import tinDB.model.v2.ResultGraph.DbResultNode
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.graph.Node

class DatabaseNode(
    identifier: String,
    val properties: HashSet<DatabaseProperty> = hashSetOf(),
    ) : Node(identifier) {

    fun hasProperty(prop : DatabaseProperty): Boolean {
        return properties.contains(prop)
    }

    fun addProperty(prop: DatabaseProperty): Boolean {
        return properties.add(prop);
    }
    fun addProperty(string: String): Boolean {
        return properties.add(DatabaseProperty(string));
    }
}