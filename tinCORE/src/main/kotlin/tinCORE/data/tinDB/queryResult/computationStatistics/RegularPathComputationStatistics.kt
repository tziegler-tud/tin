package tinCORE.data.tinDB.queryResult.computationStatistics

import jakarta.persistence.Entity


@Entity
class RegularPathComputationStatistics(
    preProcessingTimeInMs: Long = 0,
    mainProcessingTimeInMs: Long = 0,
    postProcessingTimeInMs: Long = 0,
    combinedTimeInMs: Long = 0
) : ComputationStatistics(preProcessingTimeInMs, mainProcessingTimeInMs, postProcessingTimeInMs, combinedTimeInMs) {
}