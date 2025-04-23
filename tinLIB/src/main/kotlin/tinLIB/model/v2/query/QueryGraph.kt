package tinLIB.model.v2.query

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.*
import tinLIB.model.v2.query.QueryEdge

class QueryGraph : AbstractGraph<Node, QueryEdge>() {
    override var nodes: NodeSet<Node> = NodeSet()
    override var edges = QueryEdgeSet()
    override var alphabet: Alphabet = Alphabet();

    override fun addEdge(edge: QueryEdge) : Boolean {
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

    fun addEdge(source: Node, target: Node, label: QueryEdgeLabel) : Boolean {
        return addEdge(QueryEdge(source, target, label));
    }
    fun addEdge(source: Node, target: Node, stringLabel: String) : Boolean {
        return addEdge(QueryEdge(source, target, stringLabel));
    }

    override fun containsEdge(edge: QueryEdge) : Boolean {
        val e = edge.asQueryEdge() ?: return false;
        return edges.contains(e)
    }

    override fun getEdgesWithSource(source: Node): List<QueryEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: Node): List<QueryEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: Node, target: Node): List<QueryEdge> {
        return edges.filterForSourceAndTarget(source, target);
    }

    override fun getEdgesWithLabel(label: EdgeLabel): List<QueryEdge> {
        return edges.filterForLabel(label);
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryGraph) return false

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