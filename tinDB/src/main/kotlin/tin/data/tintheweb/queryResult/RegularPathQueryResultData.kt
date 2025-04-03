package tin.data.tintheweb.queryResult

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.queryResult.QueryResult
import tin.model.queryResult.RegularPathQueryResult

class RegularPathQueryResultData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("computationStatistics") val computationStatistics: ComputationStatisticsData?,
    @JsonProperty("queryResultStatus") val regularPathQueryResultStatus: QueryResult.QueryResultStatus,
    @JsonProperty("answerSet") val answerSet: Set<AnswerTripletData>
) {
    constructor(model: RegularPathQueryResult) : this(
        id = model.id,
        computationStatistics = model.computationStatistics?.let { ComputationStatisticsData(it) },
        regularPathQueryResultStatus = model.queryResultStatus,
        answerSet = model.answerSet.map { AnswerTripletData(it) }.toSet()
    )
}