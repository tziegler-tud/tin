package tinDL.data.tintheweb.DLqueryTask

import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.data.tintheweb.queryResult.RegularPathQueryResultData
import tinDL.model.v1.queryResult.RegularPathQueryResult
import tinDL.model.v1.queryTask.QueryTask
import java.util.*

class DLRegularPathQueryTaskCompleteData(
    @JsonProperty("id") val id: Long?,
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
    @JsonProperty("queryFileIdentifier") val queryFileIdentifier: Long,
    @JsonProperty("transducerFileIdentifier") val transducerFileIdentifier: Long?,
    @JsonProperty("ontologyFileIdentifier") val ontologyFileIdentifier: Long,
    @JsonProperty("queryStatus") val queryStatus: QueryTask.QueryStatus,
    @JsonProperty("queryResult") val queryResult: RegularPathQueryResultData?,
    @JsonProperty("createdAt") val createdAt: Date
) {
    constructor(model: QueryTask) : this(
        id = model.id,
        computationProperties = ComputationPropertiesData(model.computationProperties),
        queryFileIdentifier = model.queryFileIdentifier,
        transducerFileIdentifier = model.transducerFileIdentifier,
        ontologyFileIdentifier = model.dataSourceFileIdentifier,
        queryStatus = model.queryStatus,
        queryResult = model.queryResult?.let { RegularPathQueryResultData(it.first() as RegularPathQueryResult) },
        createdAt = model.createdAt
    )

}

class DLQueryTaskCreateData(
    @JsonProperty("computationProperties") val computationProperties: ComputationPropertiesData,
    @JsonProperty("queryFileIdentifier") val queryFileIdentifier: Long,
    @JsonProperty("transducerFileIdentifier") val transducerFileIdentifier: Long?,
    @JsonProperty("ontologyFileIdentifier") val ontologyFileIdentifier: Long,
) {
    constructor(model: QueryTask) : this(
        computationProperties = ComputationPropertiesData(model.computationProperties),
        queryFileIdentifier = model.queryFileIdentifier,
        transducerFileIdentifier = model.transducerFileIdentifier,
        ontologyFileIdentifier = model.dataSourceFileIdentifier,
    )
}