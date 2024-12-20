package tin.services.Task

import org.semanticweb.owlapi.model.OWLNamedIndividual
import tin.model.v2.ResultGraph.ResultGraph
import tin.model.v2.Tasks.ComputationMode
import tin.model.v2.Tasks.OntologyVariant
import tin.model.v2.query.QueryGraph
import tin.model.v2.transducer.TransducerGraph
import tin.services.Task.Benchmark.*
import tin.services.ontology.OntologyExecutionContext.EL.ELExecutionContext
import tin.services.ontology.OntologyExecutionContext.ELHI.ELHINumericExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContext
import tin.services.ontology.OntologyExecutionContext.ExecutionContextType
import tin.services.ontology.OntologyManager
import tin.services.ontology.ResultGraph.*
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELH.ELSPLoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPALoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.ELHI.ELHISPLoopTableBuilder
import tin.services.ontology.loopTable.LoopTableBuilder.LoopTableBuilder
import kotlin.time.TimeSource

class TaskProcessor(
    private val task: Task,
    private val queryGraph: QueryGraph,
    private val transducerGraph: TransducerGraph,
    private val manager: OntologyManager
) {

    private val compConfig = task.getComputationConfiguration();
    private val runConfig = task.getRuntimeConfiguration();
    private val fileConfig = task.getFileConfiguration();

    private var benchmarkResults: TaskProcessingBenchmarkResult? = null;

    fun getTask() : Task {
        return task;
    }

    fun execute() : Pair<List<ShortestPathResult>, TaskProcessingBenchmarkResult> {
        //execute task

        val ec: ExecutionContext
        val resultGraph: ResultGraph
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

        var results: List<ShortestPathResult> = listOf()



        //create ec based on configuration
        when (runConfig.ontologyVariant) {
            OntologyVariant.ELH -> {
                ec = manager.createELExecutionContext(ExecutionContextType.ELH)
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
        }

        val dijkstraSolver = DijkstraSolver(resultGraph)


        when (compConfig.computationMode){
            ComputationMode.entailment -> {
                //given a,b and n, is the cost of a->b lower than n?
                val a = getIndividual(compConfig.individualNameA);
                val b = getIndividual(compConfig.individualNameB);
                val n = compConfig.maxCost ?: throw Error("Invalid arguments given for computation mode 'Entailment': Maximum cost not given")

                solverStartTime = timeSource.markNow()
                val shortestPathResult = dijkstraSolver.getShortestPath(a,b);
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
                val a = getIndividual(compConfig.individualNameA);
                val b = getIndividual(compConfig.individualNameB);

                solverStartTime = timeSource.markNow()
                val shortestPathResult = dijkstraSolver.getShortestPath(a,b);
                solverEndTime = timeSource.markNow()

                if(shortestPathResult == null) {
                    //no path exists

                }
                else {
                    results = mutableListOf(shortestPathResult)

                }


            }
            ComputationMode.allIndivudals -> {
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
        }

        val times = TaskProcessingResultTimes(spaStartTime, spaEndTime, spStartTime, spEndTime, resultGraphStartTime, resultGraphEndTime, solverStartTime, solverEndTime);
        val reasonerStats = TaskProcessingReasonerStats(ec.dlReasoner.getStats())
        benchmarkResults = TaskProcessingBenchmarkResult(times, reasonerStats, spaBuilderStats, spBuilderStats, resultBuilderStats)

        return Pair(results, benchmarkResults!!)
    }

    fun getBenchmarkResults(): TaskProcessingBenchmarkResult? {
        return benchmarkResults;
    }


    private fun getIndividual(name: String?) : OWLNamedIndividual {
        val a = name ?: throw Error("Invalid arguments given for computation mode 'Entailment': Individual not given")
        val indA = manager.getQueryParser().getNamedIndividual(a) ?: throw Error("Invalid arguments given for computation mode 'Entailment': Failed to find individual '$a' in ontology")
        return indA
    }



}