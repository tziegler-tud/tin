package tin.model.database

class DatabaseEdge(
        var source: DatabaseNode,
        var target: DatabaseNode,
        var label: String
) {

    override fun toString(): String {
        return String.format("(%s) -[%s]-> (%s)", source.identifier, label, target.identifier)
    }

    fun print() {
        println(this)
    }

    fun equals(otherEdge: DatabaseEdge): Boolean {
        return source.compareToExcludingEdges(otherEdge.source) &&
                target.compareToExcludingEdges(otherEdge.target) && label == otherEdge.label
    }

    fun getSource(): DatabaseNode {
        return source
    }

    fun getTarget(): DatabaseNode {
        return target
    }

    fun getLabel(): String {
        return label
    }
}