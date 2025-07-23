package tinDB.model.v2.ResultGraph

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.ResultGraph.*
import tinLIB.model.v2.graph.*

class DbResultGraph: ResultGraph<DbResultNode, DbResultEdge>() {
    override var nodes: ResultNodeSet<DbResultNode> = DbResultNodeSet()
    override var edges: ResultEdgeSet<DbResultEdge> = DbResultEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    override fun addEdge(edge: DbResultEdge) : Boolean {
        if (nodes.containsWithoutState(edge.source) && nodes.containsWithoutState(edge.target) ) {
            return edges.add(edge);
        }
        throw Error("Unable to add Edge: source or target node are not present in the graph.")
    }

    override fun addEdge(source: DbResultNode, target: DbResultNode, cost: Int): Boolean {
        return addEdge(DbResultEdge(source, target, cost))
    }

    override fun addEdge(source: DbResultNode, target: DbResultNode, label: ResultEdgeLabel): Boolean {
        return addEdge(DbResultEdge(source, target, label))
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