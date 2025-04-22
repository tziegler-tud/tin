package tinLIB.model.v2.ResultGraph

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.ResultGraph.ResultGraphIndividual
import tinLIB.model.v2.graph.*

abstract class ResultGraph<T: ResultNode, E: ResultEdge> : AbstractGraph<T,E>() {
    override var nodes: ResultNodeSet<T> = ResultNodeSet()
    override var edges: ResultEdgeSet<E> = ResultEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    override fun addEdge(edge: E) : Boolean {
        if (nodes.containsWithoutState(edge.source as T) && nodes.containsWithoutState(edge.target as T) ) {
            return edges.add(edge);
        }
        throw Error("Unable to add Edge: source or target node are not present in the graph.")
    }

    abstract fun addEdge(source: T, target: T, label: ResultEdgeLabel) : Boolean
    abstract fun addEdge(source: T, target: T, cost: Int) : Boolean

    fun getInitialNodes(individual: ResultGraphIndividual) : List<T> {
        return this.nodes.filter {
            it.individual == individual
                    && it.isInitialState;
        };
    }

    fun getFinalNodes(individual: ResultGraphIndividual) : List<T> {
        return this.nodes.filter {
            it.individual == individual
                    && it.isFinalState;
        };
    }

    override fun containsEdge(edge: E) : Boolean {
        return edges.contains(edge as ResultEdge)
    }

    override fun getEdgesWithSource(source: T): List<E> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: T): List<E> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: T, target: T): List<E> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<E> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResultGraph<*, *>) return false

        return super.equals(other);
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}