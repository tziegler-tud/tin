package tin.services.Task

import tin.model.v2.Tasks.BenchmarkResult
import tin.services.Task.Benchmark.TaskProcessingBenchmarkResult
import tin.services.ontology.ResultGraph.ShortestPathResult

class ProcessingResult(
    val processingResultStatus: ProcessingResultStatus,
    val result: List<ShortestPathResult>,
    val benchmarkResult: TaskProcessingBenchmarkResult
)