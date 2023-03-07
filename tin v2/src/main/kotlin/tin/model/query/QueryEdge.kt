package tin.model.query

class QueryEdge(
        val source: QueryNode,
        val target: QueryNode,
        val label: String
) {

    override fun toString(): String {
        val eps = "ε"
        val incoming: String = if (label.isEmpty()) {
            eps
        } else label
        return String.format("(%s) -[%3s]-> (%s)", source.identifier, incoming, target.identifier)
    }

    fun print() {
        println(this)
    }

    fun equals(otherEdge: QueryEdge): Boolean {
        return source.equalsExcludingEdges(otherEdge.source) &&
                target.equalsExcludingEdges(otherEdge.target) && label == otherEdge.label
    }
}
