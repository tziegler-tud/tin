package tinDB.model.v2.DatabaseGraph

import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.graph.*

class DatabaseGraph : AbstractGraph<DatabaseNode, DatabaseEdge>() {
    override var nodes: NodeSet<DatabaseNode> = NodeSet()
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

    fun addEdge(source: DatabaseNode, target: DatabaseNode, label: DatabaseEdgeLabel) : Boolean {
        return addEdge(DatabaseEdge(source, target, label));
    }
    fun addEdge(source: DatabaseNode, target: DatabaseNode, stringLabel: String) : Boolean {
        return addEdge(DatabaseEdge(source, target, stringLabel));
    }

    fun addNodeProperty(node: DatabaseNode, property: DatabaseProperty): Boolean {
        val n = getNode(node.identifier)
        if(n != null) {
            return node.addProperty(property)
        }
        else return false
    }

    fun addNodeProperty(node: DatabaseNode, property: String) : Boolean {
        return addNodeProperty(node, DatabaseProperty(property))
    }

    override fun containsEdge(edge: DatabaseEdge) : Boolean {
        return edges.contains(edge)
    }

    override fun getEdgesWithSource(source: DatabaseNode): List<DatabaseEdge> {
        return edges.filterForSource(source);
    }

    override fun getEdgesWithTarget(target: DatabaseNode): List<DatabaseEdge> {
        return edges.filterForTarget(target);
    }

    override fun getEdgesWithSourceAndTarget(source: DatabaseNode, target: DatabaseNode): List<DatabaseEdge> {
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