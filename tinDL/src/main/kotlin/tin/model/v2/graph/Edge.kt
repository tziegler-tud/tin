package tin.model.v2.graph

import tin.model.v2.ResultGraph.ResultEdge
import tin.model.v2.genericGraph.GenericEdge
import tin.model.v2.transducer.TransducerEdge
import tin.model.v2.query.QueryEdge

interface Edge {

    val source: Node;
    val target: Node;
    val label: EdgeLabel;

    fun print();

    fun asGenericEdge(): GenericEdge?;
    fun asTransducerEdge(): TransducerEdge?;
    fun asQueryEdge(): QueryEdge?;
    fun asResultEdge(): ResultEdge?;
}
