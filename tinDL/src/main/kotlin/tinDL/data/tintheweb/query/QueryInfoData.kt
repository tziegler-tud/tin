package tinDL.data.tintheweb.query

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.model.v2.File.TinFile
import tinLIB.model.v2.query.QueryGraph
import tinDL.services.ontology.OntologyInfoData
import java.util.*

data class QueryInfoData @JsonCreator constructor(
    @JsonProperty("filename") val filename: String,
    @JsonProperty("stateCount") val stateCount: Int,
    @JsonProperty("edgesCount") val edgeCount: Int,
    @JsonProperty("lastModified") val lastModified: Date? = null,
) {
    constructor(filename: String, queryGraph: QueryGraph): this(
        filename = filename,
        stateCount = queryGraph.nodes.size,
        edgeCount = queryGraph.edges.size,
        lastModified = null,
    )
}