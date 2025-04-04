package tinLIB.model.v1.graph

open class Edge(
    open val source: Node,
    open val target: Node,
    val label: String
) {

    override fun toString(): String {
        val eps = "epsilon"
        val incoming: String = if (label.isEmpty()) {
            eps
        } else label
        return String.format("(%s) -[%3s]-> (%s)", source.identifier, incoming, target.identifier)
    }

    fun print() {
        println(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Edge) return false

        return this.checkForNodesEquality(other) &&
                label == other.label
    }

    /**
     * call the trimmed QueryNode.equals() method in order to prevent a circular dependency.
     */
    private fun checkForNodesEquality(other: Edge): Boolean {
        return source.equalsWithoutEdges(other.source) &&
                target.equalsWithoutEdges(other.target)
    }

    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
