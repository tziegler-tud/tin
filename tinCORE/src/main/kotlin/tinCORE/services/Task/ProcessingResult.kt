package tinCORE.services.Task

import tinCORE.data.Task.DlTask.Benchmark.TaskProcessingBenchmarkResult
import tinCORE.data.Task.TaskResult

class ProcessingResult(
    val processingResultStatus: ProcessingResultStatus,
    val results: List<TaskResult>,
    val benchmarkResult: TaskProcessingBenchmarkResult?
)