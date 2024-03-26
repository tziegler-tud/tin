package tin.model.database

import tin.model.graph.Edge
import tin.model.graph.EdgeSet
import tin.model.graph.Node
import tin.model.query.QueryEdge


class DatabaseNode(
    identifier: String,
    edges: EdgeSet<DatabaseEdge> = EdgeSet(),
    var properties: HashSet<String> = HashSet()
) : Node(
        identifier, true, true, edges
){

    override var edges: EdgeSet<DatabaseEdge> = EdgeSet();

    fun addEdge(edge: DatabaseEdge) {
        edges.add(edge);
    }

    fun addProperty(property: String) {
        properties.add(property);
    }

    fun addProperties(vararg properties: String) {
        properties.forEach{
            addProperty(it);
        }
    }

    fun hasProperty(property: String) : Boolean {
        return properties.contains(property);
    }

    /**
     * plain DatabaseNode.equals() and DatabaseEdge.equals() methods will cause a circular dependency and stack overflows.
     * It is more important to check here if edges set is equal since we have to trim the equals() method in the Edge class.
     * That will resolve the circular dependency.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseNode) return false

        return this.equalsWithoutEdges(other) &&
                edges == other.edges
    }

    /**
     * we need all properties to be checked because we use this as an equals() method
     * We must not check for edges == other.edges but we can check their size to prevent at least some false positives.
     */
    override fun equalsWithoutEdges(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseNode) return false

        return identifier == other.identifier &&
                edges.size == other.edges.size &&
                properties == other.properties
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + edges.hashCode()
        return result
    }
}