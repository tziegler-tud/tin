package tinCORE.services.Task.TaskProcessor

import tinDB.model.v2.DatabaseGraph.DatabaseGraph
import tinCORE.data.Task.*
import tinCORE.data.Task.DlTask.Benchmark.*
import tinDB.model.v2.ResultGraph.DbResultGraph
import tinDB.model.v2.ResultGraph.DbResultNode
import tinDB.services.ontology.ResultGraph.DbResultGraphBuilder
import tinLIB.model.v2.ResultGraph.ResultNode

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.services.ResultGraph.DijkstraSolver
import tinLIB.services.ResultGraph.FloydWarshallSolver
import tinLIB.services.ResultGraph.ShortestPathResult
import tinLIB.services.ontology.ResultGraph.ResultGraphBuilderStats

import kotlin.time.TimeSource

class DbTaskProcessor(
    override val task: Task,
    override val queryGraph: QueryGraph,
    override val transducerMode: TransducerMode,
    override val transducerGenerationMode: TransducerGenerationMode? = null,
    override val transducerGraphProvided: TransducerGraph? = null,
    val databaseGraph: DatabaseGraph
) : AbstractTaskProcessor<DbResultNode>(
    task,
    queryGraph,
    transducerMode,
    transducerGenerationMode,
    transducerGraphProvided,
) {

    private val compConfig = task.getComputationConfiguration();
    private val runConfig = task.getRuntimeConfiguration();
    private val fileConfig = task.getFileConfiguration();

    private var benchmarkResults: TaskProcessingBenchmarkResult? = null;

    constructor(
        task: Task,
        queryGraph: QueryGraph,
        transducerGraph: TransducerGraph,
        databaseGraph: DatabaseGraph,
    ): this(task, queryGraph, TransducerMode.provided, null, transducerGraph, databaseGraph)

    override fun execute(): TaskProcessorExecutionResult<DbResultNode> {
        //execute task
        val timeSource = TimeSource.Monotonic

        val resultGraphStartTime: TimeSource.Monotonic.ValueTimeMark
        val resultGraphEndTime: TimeSource.Monotonic.ValueTimeMark
        val solverStartTime: TimeSource.Monotonic.ValueTimeMark
        val solverEndTime: TimeSource.Monotonic.ValueTimeMark


        var results: List<ShortestPathResult<DbResultNode>> = listOf()



        val transducerGraph = buildTransducerGraph(transducerMode, transducerGenerationMode, queryGraph.generateAlphabet(), databaseGraph.generateAlphabet())

        val resultGraphBuilder = DbResultGraphBuilder(queryGraph ,transducerGraph)
        resultGraphStartTime = timeSource.markNow()
        val resultGraph = resultGraphBuilder.constructResultGraph();
        resultGraphEndTime = timeSource.markNow();

        val resultBuilderStats = ResultGraphBuilderStats(resultGraph.nodes.size, resultGraph.edges.size, 0, 0, 0)

        val dijkstraSolver = DijkstraSolver(resultGraph)

        //TODO: Implement everything

        return TaskProcessorExecutionResult(
            results,
            benchmarkResults!!
        )
    }

}