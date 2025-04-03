package tinDL.services.Task

import tinDL.model.v2.Tasks.BenchmarkResult
import tinDL.services.Task.Benchmark.TaskProcessingBenchmarkResult
import tinDL.services.ontology.ResultGraph.ShortestPathResult

class ProcessingResult(
    val processingResultStatus: ProcessingResultStatus,
    val result: List<ShortestPathResult>,
    val benchmarkResult: TaskProcessingBenchmarkResult
)