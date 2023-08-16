package tin.model.database

import java.util.*

class DatabaseGraph {

    var nodes: MutableSet<DatabaseNode> = HashSet()
    var alphabet: Set<String> = HashSet()

    fun addNodes(vararg n: DatabaseNode) {
        nodes.addAll(listOf(*n))
    }

    /**
     * Note that we cannot use "nodes.add()" or similar functions that use "equals()" here.
     * This is because we call this function to populate the graph;
     * i.e. when calling this function several times after another
     * even the same node differs now from the last iteration because one edge was added.
     * We thus have to treat the comparison of nodes differently for the sake of keeping it unique outside the graph population.
     */
    fun addEdge(source: DatabaseNode, target: DatabaseNode, label: String) {
        /** check for existing source and target */
        val existingSource = findNodeWithoutEdgeComparison(source)
        val existingTarget = findNodeWithoutEdgeComparison(target)

        /** if it wasn't found: we add the new nodes.*/
        if (existingSource == null) {
            nodes.add(source)
        }

        if (existingTarget == null) {
            nodes.add(target)
        }

        val newEdge = DatabaseEdge(source, target, label)
        // don't add duplicate edges!
        for (existingEdge in source.edges) {
            if (existingEdge == newEdge) {
                return
            }
        }
        source.edges.add(newEdge)
    }
    /** helper function to use in the context of populating the graph only.
     * Note that since we cannot have different nodes with the same identifier,
     * we just have to check the identifier property and no other properties.
     */
    private fun findNodeWithoutEdgeComparison(databaseNode: DatabaseNode): DatabaseNode? {
        return nodes.find {
            it.identifier == databaseNode.identifier
        }
    }




    fun printGraph() {
        for (node in nodes) {
            for (edge in node.edges) {
                edge.print()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseGraph) return false

        return nodes == other.nodes &&
                alphabet == other.alphabet
    }

    override fun hashCode(): Int {
        var result = nodes.hashCode()
        result = 31 * result + alphabet.hashCode()
        return result
    }


}