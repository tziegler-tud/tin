package tinCORE.services.Task

import tinCORE.data.Task.DlTask.Benchmark.DlTaskProcessingBenchmarkResult
import tinCORE.data.Task.DlTask.DlTaskResult
import tinCORE.data.Task.TaskProcessingBenchmarkResult
import tinCORE.data.Task.TaskResult

class DlProcessingResult(
    override val processingResultStatus: ProcessingResultStatus,
    override val results: List<DlTaskResult>,
    override val benchmarkResult: DlTaskProcessingBenchmarkResult?,
) : ProcessingResult