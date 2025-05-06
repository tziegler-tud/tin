package tinDB.model.v2.productAutomaton

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.AbstractGraph
import tinLIB.model.v2.graph.EdgeSet
import tinLIB.model.v2.graph.NodeSet

class ProductAutomatonGraph (
    override val nodes: NodeSet<ProductAutomatonNode> = NodeSet(),
    override val edges: ProductAutomatonEdgeSet = ProductAutomatonEdgeSet(),
    override var alphabet: Alphabet = Alphabet()

) : AbstractGraph<ProductAutomatonNode, ProductAutomatonEdge>(){


    override fun addNode(node: ProductAutomatonNode): Boolean {
        return nodes.add(node)
    }

    override fun addEdge(edge: ProductAutomatonEdge) : Boolean {
        if (nodes.containsWithoutState(edge.source) && nodes.containsWithoutState(edge.target) ) {
            return edges.add(edge);
        }
        throw Error("Unable to add Edge: source or target node are not present in the graph.")
    }

}
