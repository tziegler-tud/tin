package tin.data.tintheweb.queryResult

import com.fasterxml.jackson.annotation.JsonProperty
import tin.data.tintheweb.queryTask.QueryTaskData
import tin.model.queryResult.QueryResult

class QueryResultData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("queryTask") val queryTask: QueryTaskData,
    @JsonProperty("computationStatistics") val computationStatistics: ComputationStatisticsData?,
    @JsonProperty("queryResultStatus") val queryResultStatus: QueryResult.QueryResultStatus,
    @JsonProperty("answerSet") val answerSet: Set<AnswerTripletData>
) {
    constructor(model: QueryResult) : this(
        id = model.id,
        queryTask = QueryTaskData(model.queryTask),
        computationStatistics = model.computationStatistics?.let { ComputationStatisticsData(it.preProcessingTimeInMs, it.mainProcessingTimeInMs, it.postProcessingTimeInMs ) },
        queryResultStatus = model.queryResultStatus,
        answerSet = model.answerSet.map { AnswerTripletData(it) }.toSet()
    )
}