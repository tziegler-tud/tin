package tin.data.tintheweb.transducer

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.v2.File.TinFile
import tin.model.v2.query.QueryGraph
import tin.services.ontology.OntologyInfoData
import java.util.*

data class TransducerInfoData @JsonCreator constructor(
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
