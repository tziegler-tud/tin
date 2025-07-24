package tinDB.model.v2.ResultGraph

import tinDB.model.v2.productAutomaton.ProductAutomatonEdge
import tinDB.model.v2.productAutomaton.ProductAutomatonGraph
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.ResultGraph.*
import tinLIB.model.v2.graph.*

class DbResultGraph: ResultGraph<DbResultNode, DbResultEdge>() {
    override var nodes: ResultNodeSet<DbResultNode> = DbResultNodeSet()
    override var edges: ResultEdgeSet<DbResultEdge> = DbResultEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    companion object {
        fun fromProductAutomaton(productAutomatonGraph: ProductAutomatonGraph) : DbResultGraph {
            val graph = DbResultGraph()
            for(node in productAutomatonGraph.nodes) {
                graph.nodes.add(DbResultNode(node))
            }
            for(edge in productAutomatonGraph.edges) {
                graph.addEdge(edge)
            }
            return graph
        }
    }

    override fun addEdge(edge: DbResultEdge) : Boolean {
        if (nodes.containsWithoutState(edge.source) && nodes.containsWithoutState(edge.target) ) {
            return edges.add(edge);
        }
        throw Error("Unable to add Edge: source or target node are not present in the graph.")
    }

    fun addEdge(source: DbResultNode, target: DbResultNode, label: DbResultEdgeLabel): Boolean {
        return addEdge(DbResultEdge(source, target, label))
    }

    fun addEdge(productAutomatonEdge: ProductAutomatonEdge): Boolean {
        return addEdge(DbResultEdge(productAutomatonEdge))
    }

    override fun containsEdge(edge: DbResultEdge) : Boolean {
        return edges.contains(edge as ResultEdge)
    }

    override fun getEdgesWithSource(source: DbResultNode): List<DbResultEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: DbResultNode): List<DbResultEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: DbResultNode, target: DbResultNode): List<DbResultEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<DbResultEdge> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DbResultGraph) return false

        return super.equals(other);
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}