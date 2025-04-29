package tinCORE.data.Task.DlTask.Benchmark

import tinLIB.services.ontology.ResultGraph.ResultGraphBuilderStats


class TaskProcessingBenchmarkResult(
    val times: TaskProcessingResultTimes,
    val reasonerStats: TaskProcessingReasonerStats,
    val spaBuilderStats: TaskProcessingSpaBuilderStats,
    val spBuilderStats: TaskProcessingSpBuilderStats,
    val resultBuilderStats: ResultGraphBuilderStats,
) {
}