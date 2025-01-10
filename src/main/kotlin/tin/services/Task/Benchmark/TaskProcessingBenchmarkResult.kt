package tin.services.Task.Benchmark

class TaskProcessingBenchmarkResult(
    val times: TaskProcessingResultTimes,
    val reasonerStats: TaskProcessingReasonerStats,
    val spaBuilderStats: TaskProcessingSpaBuilderStats,
    val spBuilderStats: TaskProcessingSpBuilderStats,
    val resultBuilderStats: TaskProcessingResultBuilderStats,
) {
}