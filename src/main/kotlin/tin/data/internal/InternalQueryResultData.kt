package tin.data.internal

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.technical.QueryResult
import tin.model.technical.QueryTask
import tin.model.technical.internal.ComputationStatistics
import tin.model.utils.PairOfStrings

data class InternalQueryResultData @JsonCreator constructor(
    @JsonProperty("queryTask") val queryTask: QueryTask,
    @JsonProperty("computationStatistics") val computationStatistics: ComputationStatistics?,
    @JsonProperty("queryResultStatus") val queryResultStatus: QueryResult.QueryResultStatus,
    @JsonProperty("answerMap") val answerMap: Map<PairOfStrings, Double>
) {

    fun transformInternalQueryResultDataToQueryResult(internalObject: InternalQueryResultData): QueryResult {
        val transformedAnswerMap = this.answerMap.mapKeys { entry ->
            getStringFromKey(entry.key)
        }

        return QueryResult(
            queryTask,
            computationStatistics,
            queryResultStatus,
            transformedAnswerMap
        )
    }

    fun transformQueryResultToInternalQueryResult(queryResult: QueryResult): InternalQueryResultData {
        val transformedAnswerMap = queryResult.answerMap.mapKeys { entry ->
            getKeyFromString(entry.key)
        }

        return InternalQueryResultData(
            queryResult.queryTask,
            queryResult.computationStatistics,
            queryResult.queryResultStatus,
            transformedAnswerMap
        )
    }

    private fun getStringFromKey(key: PairOfStrings): String {
        return "${key.first} -> ${key.second}"
    }

    private fun getKeyFromString(keyString: String): PairOfStrings {
        val parts = keyString.split(" -> ")
        require(parts.size == 2) { "Invalid key string format: $keyString" }
        return PairOfStrings(parts[0], parts[1])
    }

}