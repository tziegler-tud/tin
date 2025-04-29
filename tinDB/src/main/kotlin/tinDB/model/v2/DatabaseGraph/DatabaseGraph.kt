package tinDB.model.v2.DatabaseGraph

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.*

class DatabaseGraph : AbstractGraph<Node, DatabaseEdge>() {
    override var nodes: NodeSet<Node> = NodeSet()
    override var edges = DatabaseEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    override fun addEdge(edge: DatabaseEdge) : Boolean {
        /**
         * add nodes if not present
         */
        if (nodes.contains(edge.source)) {
            nodes.add(edge.source)
        }

        if (nodes.contains(edge.target)) {
            nodes.add(edge.target)
        }
        return edges.add(edge);
    }

    fun addEdge(source: Node, target: Node, label: DatabaseEdgeLabel) : Boolean {
        return addEdge(DatabaseEdge(source, target, label));
    }
    fun addEdge(source: Node, target: Node, stringLabel: String) : Boolean {
        return addEdge(DatabaseEdge(source, target, stringLabel));
    }

    override fun containsEdge(edge: DatabaseEdge) : Boolean {
        return edges.contains(edge)
    }

    override fun getEdgesWithSource(source: Node): List<DatabaseEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: Node): List<DatabaseEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<DatabaseEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<DatabaseEdge> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseGraph) return false

        return super.equals(other);
    }

    fun generateAlphabet(): Alphabet {
        val al = Alphabet();
        for (edge in edges) {
            val edgeLabel = edge.label.label
            val string = edgeLabel.getLabel();
            if (edgeLabel.isConceptAssertion()) {
                al.addConceptName(string)
            } else {
                al.addRoleName(string)
            }
        }
        alphabet = al;
        return al;
    }
}