package tin.model.v2.ResultGraph

import tin.model.v2.graph.AbstractEdge
import tin.model.v2.graph.Edge
import tin.model.v2.graph.EdgeLabelProperty
import tin.model.v2.graph.Node
import tin.model.v2.query.QueryEdge
import tin.model.v2.transducer.TransducerEdge

class ResultEdge (
    override val source: ResultNode,
    override val target: ResultNode,
    override val label: ResultEdgeLabel
) : AbstractEdge(source, target, label) {

    constructor(source: ResultNode, target: ResultNode, cost: Int) : this(source, target, ResultEdgeLabel(cost))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResultEdge) return false

        return checkForNodesEquality(other) &&
                label == other.label
    }

    private fun checkForNodesEquality(other: ResultEdge): Boolean {
        return source == other.source &&
                target == other.target
    }

    override fun asTransducerEdge(): TransducerEdge? {
        return null;
    }

    override fun asQueryEdge(): QueryEdge? {
        return null;
    }

    override fun asResultEdge(): ResultEdge {
        return this;
    }


    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
