package tinCORE.services.Task

import tinCORE.data.Task.DbTask.Benchmark.DbTaskProcessingBenchmarkResult
import tinCORE.data.Task.DbTask.DbTaskResult

class DbProcessingResult(
    override val processingResultStatus: ProcessingResultStatus,
    override val results: List<DbTaskResult>,
    override val benchmarkResult: DbTaskProcessingBenchmarkResult?,
) : ProcessingResult