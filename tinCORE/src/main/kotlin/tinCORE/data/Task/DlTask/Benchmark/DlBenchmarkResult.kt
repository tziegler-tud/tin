package tinCORE.data.Task.DlTask.Benchmark

import org.springframework.data.jpa.repository.JpaRepository

import jakarta.persistence.*
import tinCORE.data.Task.BenchmarkResult
import tinCORE.data.Task.DlTask.DlTask

@Entity
class DlBenchmarkResult(
    @OneToOne(cascade = [CascadeType.ALL])
    override val task: DlTask? = null,

    val superClassCacheSize: Int? = null,
    val equivalentClassCacheSize: Int? = null,
    val subClassCacheSize: Int? = null,
    val propertySubsumptionCacheSize: Int? = null,
    val entailmentCacheSize: Int? = null,
    val superClassCacheHitCounter: Int? = null,
    val equivNodeCacheHitCounter: Int? = null,
    val subClassCacheHitCounter: Int? = null,
    val propertySubsumptionCacheHitCounter: Int? = null,
    val entailmentCacheHitCounter: Int? = null,
    val entailmentCacheMissCounter: Int? = null,

    val spaTimeMS: Long? = null,
    val spTimeMS: Long? = null,
    val resultGraphTimeMS: Long? = null,
    val solverTimeMS: Long? = null,

    val spaTotalIterations: Int? = null,
    val spaTableSize: Int? = null,
    val spaMaxTableSize: Long? = null,

    val spTableSize: Int? = null,
    val spMaxTableSize: Int? = null,

    val rg_edges: Int? = null,
    val rg_nodes: Int? = null,
    val rg_minEdgeCost: Int? = null,
    val rg_maxEdgeCost: Int? = null,
    val rg_unreachableNodesAmount: Int? = null,
) : BenchmarkResult {
    @GeneratedValue
    @Id
    val id: Long = 0

    constructor(task: DlTask, benchmarkResult: DlTaskProcessingBenchmarkResult) : this(
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
        benchmarkResult.spaBuilderStats.maxTableSize.toLong(),

        benchmarkResult.spBuilderStats.tableSize,
        benchmarkResult.spBuilderStats.maxTableSize,

        benchmarkResult.resultBuilderStats.edges,
        benchmarkResult.resultBuilderStats.nodes,
        benchmarkResult.resultBuilderStats.minEdgeCost,
        benchmarkResult.resultBuilderStats.maxEdgeCost,
        benchmarkResult.resultBuilderStats.unreachableNodesAmount
    )

}

interface DlBenchmarkResultsRepository : JpaRepository<DlBenchmarkResult, Long>