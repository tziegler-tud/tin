package tin.model

import tin.model.query.QueryGraph

class ConjunctiveQueryGraphMap(
    private var graphs: MutableMap<String, QueryGraph>
) {

    /**
     * adds the @graph to the map with the @graphIdentifier as key.
     * @return true if the map already contained a graph with the @graphIdentifier, false otherwise.
     */
    fun addGraphToMap(graphIdentifier: String, graph: QueryGraph): Boolean {
        return graphs.put(graphIdentifier, graph) !== null
    }

    fun get(graphIdentifier: String): QueryGraph? {
        return graphs[graphIdentifier]
    }

    fun getMap(): MutableMap<String, QueryGraph> {
        return graphs
    }

    override fun equals(other: Any?): Boolean {
        return this.graphs == (other as ConjunctiveQueryGraphMap).graphs
    }

    override fun hashCode(): Int {
        return graphs.hashCode()
    }

}