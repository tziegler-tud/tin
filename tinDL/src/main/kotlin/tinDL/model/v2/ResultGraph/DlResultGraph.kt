package tinDL.model.v2.ResultGraph

import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.util.ShortFormProvider
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.ResultGraph.*
import tinLIB.model.v2.graph.*

class DlResultGraph(
    val shortFormProvider: ShortFormProvider
) : ResultGraph<DlResultNode, DlResultEdge>() {
    override var nodes: ResultNodeSet<DlResultNode> = DlResultNodeSet()
    override var edges: ResultEdgeSet<DlResultEdge> = DlResultEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    val individualFactory = DlResultGraphIndividualFactory(shortFormProvider)

    override fun addEdge(edge: DlResultEdge) : Boolean {
        if (nodes.containsWithoutState(edge.source as DlResultNode) && nodes.containsWithoutState(edge.target as DlResultNode) ) {
            return edges.add(edge as DlResultEdge);
        }
        throw Error("Unable to add Edge: source or target node are not present in the graph.")
    }

    fun addEdge(source: DlResultNode, target: DlResultNode, cost: Int): Boolean {
        return addEdge(DlResultEdge(source, target, cost))
    }

    fun addEdge(source: DlResultNode, target: DlResultNode, label: ResultEdgeLabel): Boolean {
        return addEdge(DlResultEdge(source, target, label))
    }

    fun getInitialNodes(individual: OWLNamedIndividual) : List<Node> {
        return this.nodes.filter {
            it.individual == individualFactory.fromOWLNamedIndividual(individual)
                    && it.isInitialState;
        };
    }

    fun getFinalNodes(individual: OWLNamedIndividual) : List<Node> {
        return this.nodes.filter {
            it.individual == individualFactory.fromOWLNamedIndividual(individual)
                    && it.isFinalState;
        };
    }

    override fun containsEdge(edge: DlResultEdge) : Boolean {
        return edges.contains(edge as ResultEdge)
    }

    override fun getEdgesWithSource(source: DlResultNode): List<DlResultEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: DlResultNode): List<DlResultEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: DlResultNode, target: DlResultNode): List<DlResultEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<DlResultEdge> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DlResultGraph) return false

        return super.equals(other);
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}