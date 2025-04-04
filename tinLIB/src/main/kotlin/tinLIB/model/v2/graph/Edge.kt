package tinLIB.model.v2.graph

import tinLIB.model.v2.genericGraph.GenericEdge
import tinLIB.model.v2.transducer.TransducerEdge
import tinLIB.model.v2.query.QueryEdge

interface Edge {

    val source: Node;
    val target: Node;
    val label: EdgeLabel;

    fun print();

    fun asGenericEdge(): GenericEdge?;
    fun asTransducerEdge(): TransducerEdge?;
    fun asQueryEdge(): QueryEdge?;
//    fun asResultEdge(): ResultEdge?;
}
