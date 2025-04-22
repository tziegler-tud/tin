package tinLIB.services.Task

import tinLIB.data.Task.Task
import tinLIB.data.Task.TransducerGenerationMode
import tinLIB.data.Task.TransducerMode
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph


interface TaskProcessor<T: ResultNode> {

    val task: Task
    val queryGraph: QueryGraph
    val transducerMode: TransducerMode
    val transducerGenerationMode: TransducerGenerationMode?
    val transducerGraphProvided: TransducerGraph?

    abstract fun execute() : ProcessingResult<T>
}