package tinCORE.data.Task.DlTask.Benchmark

import tinCORE.data.Task.TaskProcessingBenchmarkResult
import tinLIB.services.ontology.ResultGraph.ResultGraphBuilderStats


class DlTaskProcessingBenchmarkResult(
    val times: TaskProcessingResultTimes,
    val reasonerStats: TaskProcessingReasonerStats,
    val spaBuilderStats: TaskProcessingSpaBuilderStats,
    val spBuilderStats: TaskProcessingSpBuilderStats,
    val resultBuilderStats: ResultGraphBuilderStats,
) : TaskProcessingBenchmarkResult {
}