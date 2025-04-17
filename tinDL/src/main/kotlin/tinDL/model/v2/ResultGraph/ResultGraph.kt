package tinDL.model.v2.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tinLIB.model.v1.alphabet.Alphabet
import tinLIB.model.v2.graph.*

class ResultGraph : AbstractGraph() {
    override var nodes: ResultNodeSet = ResultNodeSet()
    override var edges = ResultEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    override fun addEdge(edge: Edge) : Boolean {
        if (nodes.containsWithoutState(edge.source as ResultNode) && nodes.containsWithoutState(edge.target as ResultNode) ) {
            return edges.add(edge as ResultEdge);
        }
        throw Error("Unable to add Edge: source or target node are not present in the graph.")
    }

    fun addEdge(source: ResultNode, target: ResultNode, label: ResultEdgeLabel) : Boolean {
        return addEdge(ResultEdge(source, target, label));
    }
    fun addEdge(source: ResultNode, target: ResultNode, cost: Int) : Boolean {
        return addEdge(ResultEdge(source, target, cost));
    }

    fun getNodesWithIndividual(individual: OWLNamedIndividual) : List<Node> {
        return this.nodes.filter { (it as ResultNode).getIndividual() == individual };
    }

    fun getInitialNodes(individual: OWLNamedIndividual) : List<Node> {
        return this.nodes.filter {
            (it as ResultNode).getIndividual() == individual
                    && it.isInitialState;
        };
    }

    fun getFinalNodes(individual: OWLNamedIndividual) : List<Node> {
        return this.nodes.filter {
            (it as ResultNode).getIndividual() == individual
                    && it.isFinalState;
        };
    }

    override fun containsEdge(edge: Edge) : Boolean {
        return edges.contains(edge as ResultEdge)
    }

    override fun getEdgesWithSource(source: Node): List<ResultEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: Node): List<ResultEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<ResultEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<ResultEdge> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResultGraph) return false

        return super.equals(other);
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}