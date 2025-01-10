package tin.model.v2.Tasks

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.v1.queryResult.computationStatistics.ComputationStatistics
import tin.model.v1.queryTask.QueryTask
import tin.services.Task.Benchmark.TaskProcessingBenchmarkResult
import tin.services.Task.Benchmark.TaskProcessingResultTimes
import tin.services.Task.ProcessingResult
import javax.persistence.*
import kotlin.time.Duration

@Entity
class BenchmarkResult(
    @OneToOne(cascade = [CascadeType.ALL])
    val task: Task,

    val superClassCacheSize: Int?,
    val equivalentClassCacheSize: Int?,
    val subClassCacheSize: Int?,
    val propertySubsumptionCacheSize: Int?,
    val entailmentCacheSize: Int?,
    val superClassCacheHitCounter: Int?,
    val equivNodeCacheHitCounter: Int?,
    val subClassCacheHitCounter: Int?,
    val propertySubsumptionCacheHitCounter: Int?,
    val entailmentCacheHitCounter: Int?,
    val entailmentCacheMissCounter: Int?,

    val spaTimeMS: Long,
    val spTimeMS: Long,
    val resultGraphTimeMS: Long,
    val solverTimeMS: Long,

    val spaTotalIterations: Int,
    val spaTableSize: Int,
    val spaMaxTableSize: ULong,

    val spTableSize: Int,
    val spMaxTableSize: Int,

    val rg_edges: Int,
    val rg_nodes: Int,
    val rg_minEdgeCost: Int,
    val rg_maxEdgeCost: Int,
    val rg_unreachableNodesAmount: Int,
) {
    @GeneratedValue
    @Id
    open val id: Long = 0

    constructor(task: Task, benchmarkResult: TaskProcessingBenchmarkResult) : this(
        task,
        benchmarkResult.reasonerStats.superClassCacheSize,
        benchmarkResult.reasonerStats.equivalentClassCacheSize,
        benchmarkResult.reasonerStats.subClassCacheSize,
        benchmarkResult.reasonerStats.propertySubsumptionCacheSize,
        benchmarkResult.reasonerStats.entailmentCacheSize,
        benchmarkResult.reasonerStats.superClassCacheHitCounter,
        benchmarkResult.reasonerStats.equivNodeCacheHitCounter,
        benchmarkResult.reasonerStats.subClassCacheHitCounter,
        benchmarkResult.reasonerStats.propertySubsumptionCacheHitCounter,
        benchmarkResult.reasonerStats.entailmentCacheHitCounter,
        benchmarkResult.reasonerStats.entailmentCacheMissCounter,

        benchmarkResult.times.spaTime.inWholeMicroseconds,
        benchmarkResult.times.spTime.inWholeMicroseconds,
        benchmarkResult.times.resultGraphTime.inWholeMicroseconds,
        benchmarkResult.times.solverTime.inWholeMicroseconds,

        benchmarkResult.spaBuilderStats.totalIterations,
        benchmarkResult.spaBuilderStats.tableSize,
        benchmarkResult.spaBuilderStats.maxTableSize,

        benchmarkResult.spBuilderStats.tableSize,
        benchmarkResult.spBuilderStats.maxTableSize,

        benchmarkResult.resultBuilderStats.edges,
        benchmarkResult.resultBuilderStats.nodes,
        benchmarkResult.resultBuilderStats.minEdgeCost,
        benchmarkResult.resultBuilderStats.maxEdgeCost,
        benchmarkResult.resultBuilderStats.unreachableNodesAmount
    )

}

interface BenchmarkResultsRepository : JpaRepository<BenchmarkResult, Long>