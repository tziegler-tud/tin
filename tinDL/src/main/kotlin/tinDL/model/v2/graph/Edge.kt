package tinDL.model.v2.graph

import tinDL.model.v2.ResultGraph.ResultEdge
import tinDL.model.v2.genericGraph.GenericEdge
import tinDL.model.v2.transducer.TransducerEdge
import tinDL.model.v2.query.QueryEdge

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
