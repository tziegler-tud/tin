package tin.model.v1.queryResult.computationStatistics

import javax.persistence.Entity

@Entity
class RegularPathComputationStatistics(
    preProcessingTimeInMs: Long,
    mainProcessingTimeInMs: Long,
    postProcessingTimeInMs: Long,
    combinedTimeInMs: Long
) : ComputationStatistics(preProcessingTimeInMs, mainProcessingTimeInMs, postProcessingTimeInMs, combinedTimeInMs) {
}