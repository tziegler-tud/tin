package tinLIB.services.Task

import tinLIB.data.Task.Task
import tinLIB.data.Task.TransducerGenerationMode
import tinLIB.data.Task.TransducerMode
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph


abstract class AbstractTaskProcessor<T: ResultNode>(
    override val task: Task,
    override val queryGraph: QueryGraph,
    override val transducerMode: TransducerMode,
    override val transducerGenerationMode: TransducerGenerationMode? = null,
    override val transducerGraphProvided: TransducerGraph? = null,
): TaskProcessor<T> {

    constructor(
        task: Task,
        queryGraph: QueryGraph,
        transducerGraph: TransducerGraph,
    ): this(task, queryGraph, TransducerMode.provided, null, transducerGraph)

    abstract override fun execute() : ProcessingResult<T>
}