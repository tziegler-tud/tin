package tinCORE.services.Task.TaskProcessor

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tinCORE.data.Task.*
import tinCORE.data.Task.DlTask.Benchmark.*


import tinDL.model.v2.ResultGraph.DlResultGraph
import tinDL.services.internal.utils.DLTransducerFactory
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContext
import tinDL.services.ontology.OntologyExecutionContext.ExecutionContextType
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPALoopTableBuilder
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPLoopTableBuilder
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tinDL.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPLoopTableBuilder

import tinDL.model.v2.ResultGraph.DlResultNode
import tinDL.services.ontology.ResultGraph.ELResultGraphBuilder
import tinDL.services.ontology.ResultGraph.ELHIResultGraphBuilder

import tinDL.model.v2.ResultGraph.DlResultGraphIndividualFactory
import tinDL.services.ontology.ResultGraph.TaskProcessingResultBuilderStats

import tinLIB.model.v2.query.QueryGraph
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.services.ResultGraph.DijkstraSolver
import tinLIB.services.ResultGraph.FloydWarshallSolver
import tinLIB.services.ResultGraph.ShortestPathResult

import kotlin.time.TimeSource

class DlTaskProcessor(
    override val task: Task,
    private val manager: OntologyManager,
    override val queryGraph: QueryGraph,
    override val transducerMode: TransducerMode,
    override val transducerGenerationMode: TransducerGenerationMode? = null,
    override val transducerGraphProvided: TransducerGraph? = null,
) : AbstractTaskProcessor<DlResultNode>(
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
        manager: OntologyManager,
        queryGraph: QueryGraph,
        transducerGraph: TransducerGraph,
    ): this(task, manager, queryGraph, TransducerMode.provided, null, transducerGraph)



    override fun execute(): TaskProcessorExecutionResult<DlResultNode> {
        //execute task
        val ec: ExecutionContext
        val resultGraph: DlResultGraph
        val timeSource = TimeSource.Monotonic

        val spaStartTime: TimeSource.Monotonic.ValueTimeMark
        val spaEndTime: TimeSource.Monotonic.ValueTimeMark
        val spStartTime: TimeSource.Monotonic.ValueTimeMark
        val spEndTime: TimeSource.Monotonic.ValueTimeMark
        val resultGraphStartTime: TimeSource.Monotonic.ValueTimeMark
        val resultGraphEndTime: TimeSource.Monotonic.ValueTimeMark
        val solverStartTime: TimeSource.Monotonic.ValueTimeMark
        val solverEndTime: TimeSource.Monotonic.ValueTimeMark

        val spaBuilderStats: TaskProcessingSpaBuilderStats
        val spBuilderStats: TaskProcessingSpBuilderStats
        val resultBuilderStats: TaskProcessingResultBuilderStats

        var results: List<ShortestPathResult<DlResultNode>> = listOf()



        //create ec based on configuration
        when (runConfig.ontologyVariant) {
            OntologyVariant.ELH -> {
                ec = manager.createELExecutionContext(ExecutionContextType.ELH)
                val transducerGraph = buildTransducerGraph(transducerMode, transducerGenerationMode, transducerGraphProvided, ec, queryGraph)
                val spaBuilder = ELSPALoopTableBuilder(queryGraph, transducerGraph, manager, ec);
                val spBuilder = ELSPLoopTableBuilder(queryGraph, transducerGraph, manager, ec);

                spaStartTime = timeSource.markNow();
                val spaTable = spaBuilder.calculateFullTable();
                spaEndTime = timeSource.markNow();

                spStartTime = timeSource.markNow();
                val spTable = spBuilder.calculateFullTable(spaTable);
                spEndTime = timeSource.markNow();

                val resultGraphBuilder = ELResultGraphBuilder(ec, queryGraph ,transducerGraph)
                resultGraphStartTime = timeSource.markNow()
                resultGraph = resultGraphBuilder.constructResultGraph(spTable);
                resultGraphEndTime = timeSource.markNow();

                spaBuilderStats = TaskProcessingSpaBuilderStats(spaBuilder.statsTotalIterations, spaBuilder.statsTotalSize, spaBuilder.statsMaxPossibleSize)
                spBuilderStats = TaskProcessingSpBuilderStats(spBuilder.statsTotalSize, spBuilder.statsMaxPossibleSize)
                resultBuilderStats = TaskProcessingResultBuilderStats(resultGraph.nodes.size, resultGraph.edges.size, 0, 0, 0)

            }
            OntologyVariant.ELHI -> {
                ec = manager.createELHINumericExecutionContext()
                val transducerGraph = buildTransducerGraph(transducerMode, transducerGenerationMode, transducerGraphProvided, ec, queryGraph)
                val spaBuilder = ELHISPALoopTableBuilder(queryGraph, transducerGraph, manager, ec);
                val spBuilder = ELHISPLoopTableBuilder(queryGraph, transducerGraph, manager, ec);

                spaStartTime = timeSource.markNow();
                val spaTable = spaBuilder.calculateFullTable();
                spaEndTime = timeSource.markNow();

                spStartTime = timeSource.markNow();
                val spTable = spBuilder.calculateFullTable(spaTable);
                spEndTime = timeSource.markNow();

                val resultGraphBuilder = ELHIResultGraphBuilder(ec, queryGraph ,transducerGraph)
                resultGraphStartTime = timeSource.markNow()
                resultGraph = resultGraphBuilder.constructResultGraph(spTable);
                resultGraphEndTime = timeSource.markNow();

                spaBuilderStats = TaskProcessingSpaBuilderStats(spaBuilder.statsTotalIterations, spaBuilder.statsTotalSize, spaBuilder.statsMaxPossibleSize)
                spBuilderStats = TaskProcessingSpBuilderStats(spBuilder.statsTotalSize, spBuilder.statsMaxPossibleSize)
                resultBuilderStats = TaskProcessingResultBuilderStats(resultGraph.nodes.size, resultGraph.edges.size, 0, 0, 0)


            }

            OntologyVariant.UNSET -> {
                throw IllegalArgumentException("Failed to process task: Ontology Variant not set.")
            }
        }

        val individualFactory = DlResultGraphIndividualFactory(ec.shortFormProvider);

        val dijkstraSolver = DijkstraSolver(resultGraph)


        when (compConfig.computationMode){
            ComputationMode.entailment -> {
                //given a,b and n, is the cost of a->b lower than n?

                val a = getIndividual(compConfig.individualNameA)
                val b = getIndividual(compConfig.individualNameB)

                val n = compConfig.maxCost ?: throw Error("Invalid arguments given for computation mode 'Entailment': Maximum cost not given")

                solverStartTime = timeSource.markNow()
                val shortestPathResult = dijkstraSolver.getShortestPath(individualFactory.fromOWLNamedIndividual(a),individualFactory.fromOWLNamedIndividual(b));
                solverEndTime = timeSource.markNow()
                if(shortestPathResult == null) {
                    //no path exists

                }
                else {
                    results = mutableListOf(shortestPathResult)
                }

            }
            ComputationMode.costComputation -> {
                //given a,b, compute n
                val a = getIndividual(compConfig.individualNameA)
                val b = getIndividual(compConfig.individualNameB)

                solverStartTime = timeSource.markNow()
                val shortestPathResult = dijkstraSolver.getShortestPath(individualFactory.fromOWLNamedIndividual(a),individualFactory.fromOWLNamedIndividual(b));
                solverEndTime = timeSource.markNow()

                if(shortestPathResult == null) {
                    //no path exists

                }
                else {
                    results = mutableListOf(shortestPathResult)

                }


            }
            ComputationMode.allIndividuals -> {
                //return all pairs of individuals and their cost if <infty
                val fwSolver = FloydWarshallSolver(resultGraph)

                solverStartTime = timeSource.markNow()
                val result = fwSolver.getAllShortestPaths()
                solverEndTime = timeSource.markNow()

                results = result;
            }

            ComputationMode.allWithMaxCost -> {
                //given n, return all pairs of individuals with cost lower n
                val n = compConfig.maxCost ?: throw Error("Invalid arguments given for computation mode 'Entailment': Maximum cost not given")
                val fwSolver = FloydWarshallSolver(resultGraph)

                solverStartTime = timeSource.markNow()
                val result = fwSolver.getAllShortestPathsWithMaxCost(n);
                solverEndTime = timeSource.markNow()

                results = result;


            }

            ComputationMode.UNSET -> {
                throw IllegalArgumentException("Failed to process task: Computation mode not set.")

            }
        }

        val times = TaskProcessingResultTimes(spaStartTime, spaEndTime, spStartTime, spEndTime, resultGraphStartTime, resultGraphEndTime, solverStartTime, solverEndTime);
        val reasonerStats = TaskProcessingReasonerStats(ec.dlReasoner.getStats())
        benchmarkResults = TaskProcessingBenchmarkResult(times, reasonerStats, spaBuilderStats, spBuilderStats, resultBuilderStats)

        return TaskProcessorExecutionResult(
            results,
            benchmarkResults!!
        )
    }

    fun getBenchmarkResults(): TaskProcessingBenchmarkResult? {
        return benchmarkResults;
    }


    private fun getIndividual(name: String?) : OWLNamedIndividual {
        val a = name ?: throw Error("Invalid arguments given for computation mode 'Entailment': Individual not given")
        val indA = manager.getQueryParser().getNamedIndividual(a) ?: throw Error("Invalid arguments given for computation mode 'Entailment': Failed to find individual '$a' in ontology")
        return indA
    }

    private fun buildTransducerGraph(transducerMode: TransducerMode, transducerGenerationMode: TransducerGenerationMode?, provided: TransducerGraph?, ec: ExecutionContext, queryGraph: QueryGraph) : TransducerGraph {
        if(transducerMode == TransducerMode.provided){
            if(transducerGraphProvided != null) {
                return transducerGraphProvided
            }
            else {
                throw IllegalArgumentException("TransducerMode set to provided, but no graph was obtained.")
            }
        }
        else {
            if(transducerGenerationMode == null) throw IllegalArgumentException("TransducerMode set to generated, but TransducerGenerationMode was not given.")
            when (transducerGenerationMode) {
                TransducerGenerationMode.classicAnswers -> {
                    return DLTransducerFactory.generateClassicAnswersTransducer(ec)
                }
                TransducerGenerationMode.wordEditDistance -> {
                    return DLTransducerFactory.generateEditDistanceTransducer(queryGraph = queryGraph, ec = ec, useSimpleWeights = false);
                }
                TransducerGenerationMode.simpleEditDistance -> {
                    return DLTransducerFactory.generateEditDistanceTransducer(queryGraph = queryGraph, ec = ec, useSimpleWeights = true);
                }
            }
        }
    }



}