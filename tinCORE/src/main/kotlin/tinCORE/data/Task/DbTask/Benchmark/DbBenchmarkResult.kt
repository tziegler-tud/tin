package tinCORE.data.Task.DbTask.Benchmark

import org.springframework.data.jpa.repository.JpaRepository

import jakarta.persistence.*
import tinCORE.data.Task.BenchmarkResult
import tinCORE.data.Task.DbTask.DbTask

@Entity
class DbBenchmarkResult(
    @OneToOne(cascade = [CascadeType.ALL])
    override val task: DbTask? = null,
) : BenchmarkResult {
    @GeneratedValue
    @Id
    val id: Long = 0

    constructor(task: DbTask, benchmarkResult: DbTaskProcessingBenchmarkResult) : this(
        task,
    )

}

interface DlBenchmarkResultsRepository : JpaRepository<DbBenchmarkResult, Long>