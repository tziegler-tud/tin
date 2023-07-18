package tin.data.tintheweb.queryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.queryTask.QueryTask

class QueryTaskData(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
    @JsonProperty("queryFileIdentifier") val queryFileIdentifier: Long,
    @JsonProperty("transducerFileIdentifier") val transducerFileIdentifier: Long?,
    @JsonProperty("databaseFileIdentifier") val databaseFileIdentifier: Long,
) {
    constructor(model: QueryTask) : this(
        id = model.id,
        computationProperties = ComputationPropertiesData(model.computationProperties),
        queryFileIdentifier = model.queryFileIdentifier,
        transducerFileIdentifier = model.transducerFileIdentifier,
        databaseFileIdentifier = model.databaseFileIdentifier,
    )
}