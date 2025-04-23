package tinCORE.services.Task.TaskProcessor


import tinLIB.model.v2.alphabet.Alphabet
import tinCORE.data.Task.Task
import tinCORE.data.Task.TransducerGenerationMode
import tinCORE.data.Task.TransducerMode
import tinCORE.services.Task.ProcessingResult
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph


interface TaskProcessor<T: ResultNode> {

    val task: Task
    val queryGraph: QueryGraph
    val transducerMode: TransducerMode
    val transducerGenerationMode: TransducerGenerationMode?
    val transducerGraphProvided: TransducerGraph?

    fun execute() : TaskProcessorExecutionResult<T>
    fun buildTransducerGraph(transducerMode: TransducerMode, transducerGenerationMode: TransducerGenerationMode?, queryAlphabet: Alphabet, dataAlphabet: Alphabet) : TransducerGraph
}