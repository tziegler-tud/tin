package tinCORE.services.Task.TaskProcessor

import tinCORE.data.Task.Task
import tinCORE.data.Task.TransducerGenerationMode
import tinCORE.data.Task.TransducerMode
import tinCORE.services.Task.ProcessingResult

import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.model.v2.alphabet.Alphabet
import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph

import tinLIB.services.internal.utils.TransducerFactory


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

    abstract override fun execute() : TaskProcessorExecutionResult<T>

    override fun buildTransducerGraph(transducerMode: TransducerMode, transducerGenerationMode: TransducerGenerationMode?, queryAlphabet: Alphabet, dataAlphabet: Alphabet) : TransducerGraph {
        if(transducerMode == TransducerMode.provided) {
            if (transducerGraphProvided !== null) {
                return transducerGraphProvided!!
            } else {
                throw IllegalArgumentException("TransducerMode set to provided, but no graph was obtained.")
            }
        }
        else {
            if(transducerGenerationMode == null) throw IllegalArgumentException("TransducerMode set to generated, but TransducerGenerationMode was not given.")
            when (transducerGenerationMode) {
                TransducerGenerationMode.classicAnswers -> {
                    return TransducerFactory.generateClassicAnswersTransducer(queryAlphabet)
                }
                TransducerGenerationMode.wordEditDistance -> {
                    return TransducerFactory.generateEditDistanceTransducer(queryAlphabet = queryAlphabet, dataAlphabet= dataAlphabet, useSimpleWeights = false);
                }
                TransducerGenerationMode.simpleEditDistance -> {
                    return TransducerFactory.generateEditDistanceTransducer(queryAlphabet = queryAlphabet, dataAlphabet= dataAlphabet, useSimpleWeights = true);
                }
            }
        }
    }

//    fun buildTransducerGraph(transducerMode: TransducerMode, transducerGenerationMode: TransducerGenerationMode?, queryGraph: QueryGraph) : TransducerGraph
//    {
//        if(transducerMode == TransducerMode.provided){
//            if(transducerGraphProvided !== null) {
//                return transducerGraphProvided!!
//            }
//            else {
//                throw IllegalArgumentException("TransducerMode set to provided, but no graph was obtained.")
//            }
//        }
//        else {
//            if(transducerGenerationMode == null) throw IllegalArgumentException("TransducerMode set to generated, but TransducerGenerationMode was not given.")
//            when (transducerGenerationMode) {
//                TransducerGenerationMode.classicAnswers -> {
//                    return DLTransducerFactory.generateClassicAnswersTransducer(ec)
//                }
//                TransducerGenerationMode.wordEditDistance -> {
//                    return DLTransducerFactory.generateEditDistanceTransducer(queryGraph = queryGraph, ec = ec, useSimpleWeights = false);
//                }
//                TransducerGenerationMode.simpleEditDistance -> {
//                    return DLTransducerFactory.generateEditDistanceTransducer(queryGraph = queryGraph, ec = ec, useSimpleWeights = true);
//                }
//            }
//        }
//    }
}