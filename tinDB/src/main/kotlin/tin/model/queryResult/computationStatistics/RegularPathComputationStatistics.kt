package tin.model.queryResult.computationStatistics

import jakarta.persistence.Entity


@Entity
class RegularPathComputationStatistics(
    preProcessingTimeInMs: Long,
    mainProcessingTimeInMs: Long,
    postProcessingTimeInMs: Long,
    combinedTimeInMs: Long
) : ComputationStatistics(preProcessingTimeInMs, mainProcessingTimeInMs, postProcessingTimeInMs, combinedTimeInMs) {
}