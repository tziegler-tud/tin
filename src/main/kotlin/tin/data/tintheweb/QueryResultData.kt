package tin.data.tintheweb

import com.fasterxml.jackson.annotation.JsonProperty
import tin.data.internal.ComputationStatisticsData
import tin.model.technical.QueryResult

class QueryResultData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("queryTask") val queryTask: QueryTaskData,
    @JsonProperty("computationStatistics") val computationStatistics: ComputationStatisticsData?,
    @JsonProperty("queryResultStatus") val queryResultStatus: QueryResult.QueryResultStatus,
    @JsonProperty("answerMap") val answerMap: Map<String, Double>
) {
    constructor(model: QueryResult) : this(
        id = model.id,
        queryTask = QueryTaskData(model.queryTask),
        computationStatistics = model.computationStatistics?.let {
            ComputationStatisticsData(
                it.preProcessingTimeInMs,
                it.mainProcessingTimeInMs,
                it.postProcessingTimeInMs
            )
        },
        queryResultStatus = model.queryResultStatus,
        answerMap = model.answerMap
    )
}