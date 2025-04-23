package tinCORE.data.Task.DlTask.Benchmark

import tinDL.services.ontology.ResultGraph.TaskProcessingResultBuilderStats

class TaskProcessingBenchmarkResult(
    val times: TaskProcessingResultTimes,
    val reasonerStats: TaskProcessingReasonerStats,
    val spaBuilderStats: TaskProcessingSpaBuilderStats,
    val spBuilderStats: TaskProcessingSpBuilderStats,
    val resultBuilderStats: TaskProcessingResultBuilderStats,
) {
}