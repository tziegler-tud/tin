package tinCORE.data.tinDB.queryResult.computationStatistics

import jakarta.persistence.Entity

/**
 * @param preProcessingTimeInMs building the ConjunctiveQueryDataProvider
 * @param mainProcessingTimeInMs calculateDijkstra for each RPQ atom and finally reassemble the results
 * @param postProcessingTimeInMs sort the results by cost ascending
 * @param combinedTimeInMs sum of all of the above
 * @param combinedRPQPreProcessingTimeInMs sum of all product automata constructions for each RPQ atom
 * @param combinedRPQMainProcessingTimeInMs sum of all dijkstra runs for each RPQ atom
 * @param combinedRPQPostProcessingTimeInMs make answerSet readable and potentially sort out answers outside topK or threshold range
 * @param combinedRPQTimeInMs sum of all RPQ timings of the above
 * @param reassemblyTimeInMs reassembling all RPQ results into the C2RPQ result
 */
@Entity
class ConjunctiveComputationStatistics(
    preProcessingTimeInMs: Long = 0,
    mainProcessingTimeInMs: Long = 0,
    postProcessingTimeInMs: Long = 0,
    combinedTimeInMs: Long = 0,
    val combinedRPQPreProcessingTimeInMs: Long = 0,
    val combinedRPQMainProcessingTimeInMs: Long = 0,
    val combinedRPQPostProcessingTimeInMs: Long = 0,
    val combinedRPQTimeInMs: Long = 0,
    val reassemblyTimeInMs: Long = 0

) : ComputationStatistics(
    preProcessingTimeInMs = preProcessingTimeInMs,
    mainProcessingTimeInMs = mainProcessingTimeInMs,
    postProcessingTimeInMs = postProcessingTimeInMs,
    combinedTimeInMs = combinedTimeInMs
) {
}