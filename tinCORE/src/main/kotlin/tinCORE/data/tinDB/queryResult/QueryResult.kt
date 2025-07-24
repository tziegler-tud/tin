package tinCORE.data.tinDB.queryResult

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import tinCORE.data.Task.DbTask.DbTask
import tinCORE.data.tinDB.queryResult.computationStatistics.ComputationStatistics

@Entity
abstract class QueryResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    open val queryTask: DbTask? = null,

    @OneToOne(cascade = [CascadeType.ALL])
    open var computationStatistics: ComputationStatistics? = null,

    open val queryResultStatus: QueryResultStatus? = null,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0
}

interface QueryResultRepository : JpaRepository<QueryResult, Long> {
    fun findAllByQueryTask(queryTask: DbTask): List<QueryResult>
}