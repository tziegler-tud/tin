package tinDB.data.internal

/**
 * @param preProcessingTimeInMs building the ConjunctiveQueryDataProvider
 * @param mainProcessingTimeInMs calculateDijkstra for each RPQ atom and finally reassemble the results
 * @param postProcessingTimeInMs sort the results by cost ascending
 * @param combinedTimeInMs sum of all of the above
 * @param combinedRPQPreProcessingTimeInMs sum of all product automata constructions for each RPQ atom
 * @param combinedRPQMainProcessingTimeInMs sum of all dijkstra runs for each RPQ atom
 * @param combinedRPQPostProcessingTimeInMs make answerSet readable and potentially sort out answers outside topK or threshold range
 * @param combinedRPQTimeInMs sum of all RPQ timings of the above
 * @param combinedRPQInternalPostProcessingTimeInMs storing the RPQ result in the database. This is rather technical and not accounted for any RPQ timings. However, it is included in the C2RPQ main processing time. It is separated because for small samples it took more than 50% (~50ms) of the total time. It is to be observed whether this is a constant or linear factor.
 * @param reassemblyTimeInMs reassembling all RPQ results into the C2RPQ result
 */
data class ConjunctiveComputationStatisticsData(
    var preProcessingTimeInMs: Long,
    var mainProcessingTimeInMs: Long,
    var postProcessingTimeInMs: Long,
    var combinedTimeInMs: Long,
    var combinedRPQPreProcessingTimeInMs: Long,
    var combinedRPQMainProcessingTimeInMs: Long,
    var combinedRPQPostProcessingTimeInMs: Long,
    var combinedRPQInternalPostProcessingTimeInMs: Long,
    var combinedRPQTimeInMs: Long,
    var reassemblyTimeInMs: Long
)
