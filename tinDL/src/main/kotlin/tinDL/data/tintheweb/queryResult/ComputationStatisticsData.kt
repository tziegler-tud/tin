package tinDL.data.tintheweb.queryResult

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.model.v1.queryResult.computationStatistics.ComputationStatistics

data class ComputationStatisticsData @JsonCreator constructor(
    @JsonProperty("preProcessingTimeInMs") val preProcessingTimeInMs: Long,
    @JsonProperty("mainProcessingTimeInMs") val mainProcessingTimeInMs: Long,
    @JsonProperty("postProcessingTimeInMs") val postProcessingTimeInMs: Long,
    @JsonProperty("combinedTimeInMs") val combinedTimeInMs: Long,
) {
    constructor(model: ComputationStatistics) : this(
        preProcessingTimeInMs = model.preProcessingTimeInMs,
        mainProcessingTimeInMs = model.mainProcessingTimeInMs,
        postProcessingTimeInMs = model.postProcessingTimeInMs,
        combinedTimeInMs = model.combinedTimeInMs
    )
}
