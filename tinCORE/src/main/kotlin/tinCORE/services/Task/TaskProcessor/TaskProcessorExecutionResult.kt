package tinCORE.services.Task.TaskProcessor

import tinCORE.data.Task.TaskProcessingBenchmarkResult
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.services.ResultGraph.ShortestPathResult


class TaskProcessorExecutionResult<T: ResultNode>(
    val results: List<ShortestPathResult<T>>,
    val benchmarkResult: TaskProcessingBenchmarkResult
)