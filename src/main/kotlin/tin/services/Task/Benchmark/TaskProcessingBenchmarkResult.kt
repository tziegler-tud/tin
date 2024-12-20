package tin.services.Task.Benchmark

class TaskProcessingBenchmarkResult(
    private val times: TaskProcessingResultTimes,
    private val reasonerStats: TaskProcessingReasonerStats,
    private val spaBuilderStats: TaskProcessingSpaBuilderStats,
    private val spBuilderStats: TaskProcessingSpBuilderStats,
    private val resultBuilderStats: TaskProcessingResultBuilderStats,
) {
}