package tinLIB.services.Task

import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.services.ResultGraph.ShortestPathResult

class ProcessingResult<T: ResultNode>(
    val processingResultStatus: ProcessingResultStatus,
    val result: List<ShortestPathResult<T>>,
//    val benchmarkResult: TaskProcessingBenchmarkResult?
)