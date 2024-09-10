package tin.model.v2.graph

import tin.model.v2.query.EdgeLabel
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.query.QueryEdge

open class Edge(
    open val source: Node,
    open val target: Node,
    val label: EdgeLabel
) {

    override fun toString(): String {
        return "(${source.identifier}) - [${label}] - (${target.identifier})";
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
        return source == other.source &&
                target == other.target
    }

    override fun hashCode(): Int {
        var result = source.identifier.hashCode()
        result = 31 * result + target.identifier.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }

    public fun asQueryEdge() : QueryEdge {
        if (this is QueryEdge) return this;
        return QueryEdge(source, target, label);
    }

    public fun asTransducerEdge() : TransducerEdge {
        if (this is TransducerEdge) return this;
        throw Error("Cannot obtain TransducerEdge from non-transducer Edge instances.")
    }
}
