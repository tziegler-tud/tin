package tinCORE.services.Task

import tinCORE.data.Task.TaskProcessingBenchmarkResult
import tinCORE.data.Task.TaskResult

interface ProcessingResult {
    val processingResultStatus: ProcessingResultStatus
    val results: List<TaskResult>
    val benchmarkResult: TaskProcessingBenchmarkResult?
}