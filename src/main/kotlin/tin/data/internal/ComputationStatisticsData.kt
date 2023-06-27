package tin.data.internal

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ComputationStatisticsData @JsonCreator constructor(
    @JsonProperty("preProcessingTimeInMs") val preProcessingTimeInMs: Long,
    @JsonProperty("mainProcessingTimeInMs") val mainProcessingTimeInMs: Long,
    @JsonProperty("postProcessingTimeInMs") val postProcessingTimeInMs: Long,
)
