package tinLIB.model.v1.database

import tinLIB.model.v1.graph.Edge

class DatabaseEdge(
    override var source: DatabaseNode,
    override var target: DatabaseNode,
    label: String
) : Edge(
        source, target, label
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseEdge) return false

        return this.checkForNodesEquality(other) &&
                label == other.label
    }

    /**
     * call the trimmed QueryNode.equals() method in order to prevent a circular dependency.
     */
    private fun checkForNodesEquality(other: DatabaseEdge): Boolean {
        return source.equalsWithoutEdges(other.source) &&
                target.equalsWithoutEdges(other.target)
    }

    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }

    override fun toString(): String {
        return String.format("(%s) -[%s]-> (%s)", source.identifier, label, target.identifier)
    }
}