package tin.data.tintheweb

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.technical.QueryTask

class QueryTaskData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("computationMode") val computationMode: ComputationModeData,
    @JsonProperty("queryFileIdentifier") val queryFileIdentifier: Long,
    @JsonProperty("transducerFileIdentifier") val transducerFileIdentifier: Long?,
    @JsonProperty("databaseFileIdentifier") val databaseFileIdentifier: Long,
) {
    constructor(model: QueryTask) : this(
        id = model.id,
        computationMode = ComputationModeData(model.computationMode),
        queryFileIdentifier = model.queryFileIdentifier,
        transducerFileIdentifier = model.transducerFileIdentifier,
        databaseFileIdentifier = model.databaseFileIdentifier,
    )
}