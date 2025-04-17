package tinDL.model.v2.Tasks

import org.springframework.data.jpa.repository.JpaRepository
import tinDL.services.Task.Benchmark.TaskProcessingBenchmarkResult
import jakarta.persistence.*

@Entity
class BenchmarkResult(
    @OneToOne(cascade = [CascadeType.ALL])
    val task: Task,

    val superClassCacheSize: Int? = 0,
    val equivalentClassCacheSize: Int? = 0,
    val subClassCacheSize: Int? = 0,
    val propertySubsumptionCacheSize: Int? = 0,
    val entailmentCacheSize: Int? = 0,
    val superClassCacheHitCounter: Int? = 0,
    val equivNodeCacheHitCounter: Int? = 0,
    val subClassCacheHitCounter: Int? = 0,
    val propertySubsumptionCacheHitCounter: Int? = 0,
    val entailmentCacheHitCounter: Int? = 0,
    val entailmentCacheMissCounter: Int? = 0,

    val spaTimeMS: Long = 0,
    val spTimeMS: Long = 0,
    val resultGraphTimeMS: Long = 0,
    val solverTimeMS: Long = 0,

    val spaTotalIterations: Int = 0,
    val spaTableSize: Int = 0,
    val spaMaxTableSize: ULong = 0UL,

    val spTableSize: Int = 0,
    val spMaxTableSize: Int = 0,

    val rg_edges: Int = 0,
    val rg_nodes: Int = 0,
    val rg_minEdgeCost: Int = 0,
    val rg_maxEdgeCost: Int = 0,
    val rg_unreachableNodesAmount: Int = 0,
) {
    @GeneratedValue
    @Id
    val id: Long = 0

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

    constructor() : this(Task())
}

interface BenchmarkResultsRepository : JpaRepository<BenchmarkResult, Long>