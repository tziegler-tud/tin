package tinCORE.data.api.transducer

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tinLIB.model.v2.transducer.TransducerGraph
import java.util.*

data class TransducerInfoData @JsonCreator constructor(
    @JsonProperty("filename") val filename: String,
    @JsonProperty("stateCount") val stateCount: Int,
    @JsonProperty("edgesCount") val edgeCount: Int,
    @JsonProperty("lastModified") val lastModified: Date? = null,
) {
    constructor(filename: String, graph: TransducerGraph): this(
        filename = filename,
        stateCount = graph.nodes.size,
        edgeCount = graph.edges.size,
        lastModified = null,
    )
}