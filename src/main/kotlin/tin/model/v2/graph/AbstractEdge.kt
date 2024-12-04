package tin.model.v2.graph

import tin.model.v2.ResultGraph.ResultEdge
import tin.model.v2.genericGraph.GenericEdge
import tin.model.v2.query.QueryEdge
import tin.model.v2.query.QueryEdgeLabel
import tin.model.v2.transducer.TransducerEdge

abstract class AbstractEdge(
    override val source: Node,
    override val target: Node,
    override val label: EdgeLabel
) : Edge {

    fun checkForNodesEquality(other: GenericEdge): Boolean {
        return source == other.source &&
                target == other.target
    }

    override fun asGenericEdge(): GenericEdge? {
        return null;
    }

    override fun asQueryEdge(): QueryEdge? {
        return null;
    }

    override fun asTransducerEdge(): TransducerEdge? {
        return null;
    }

    override fun asResultEdge(): ResultEdge? {
        return null;
    }

    override fun toString(): String {
        return "(${source.identifier}) - [${label}] - (${target.identifier})";
    }

    override fun print(){
        println(this);
    }
}