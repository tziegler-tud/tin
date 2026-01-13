package tinDB.data.tintheweb.queryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tinDB.data.tintheweb.queryResult.RegularPathQueryResultData
import tinDB.model.v1.queryResult.RegularPathQueryResult
import tinDB.model.v1.queryTask.QueryTask
import java.util.*

class RegularPathQueryTaskCompleteData(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
    @JsonProperty("queryFileIdentifier") val queryFileIdentifier: Long,
    @JsonProperty("transducerFileIdentifier") val transducerFileIdentifier: Long?,
    @JsonProperty("databaseFileIdentifier") val databaseFileIdentifier: Long,
    @JsonProperty("queryStatus") val queryStatus: QueryTask.QueryStatus,
    @JsonProperty("queryResult") val queryResult: RegularPathQueryResultData?,
    @JsonProperty("createdAt") val createdAt: Date
) {
    constructor(model: QueryTask) : this(
        id = model.id,
        computationProperties = ComputationPropertiesData(model.computationProperties),
        queryFileIdentifier = model.queryFileIdentifier,
        transducerFileIdentifier = model.transducerFileIdentifier,
        databaseFileIdentifier = model.dataSourceFileIdentifier,
        queryStatus = model.queryStatus,
        queryResult = model.queryResult?.let { RegularPathQueryResultData(it.first() as RegularPathQueryResult) },
        createdAt = model.createdAt
    )

}

class QueryTaskCreateData(
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
    @JsonProperty("queryFileIdentifier") val queryFileIdentifier: Long,
    @JsonProperty("transducerFileIdentifier") val transducerFileIdentifier: Long?,
    @JsonProperty("databaseFileIdentifier") val databaseFileIdentifier: Long,
) {
    constructor(model: QueryTask) : this(
        computationProperties = ComputationPropertiesData(model.computationProperties),
        queryFileIdentifier = model.queryFileIdentifier,
        transducerFileIdentifier = model.transducerFileIdentifier,
        databaseFileIdentifier = model.dataSourceFileIdentifier,
    )

}