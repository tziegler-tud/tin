package tin.model.v2.queryResult

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.v1.queryResult.computationStatistics.ComputationStatistics
import tin.model.v2.Tasks.DlQueryTask
import tin.model.v2.queryResult.computationStatistics.DlComputationStatistics
import javax.persistence.*

@Entity
abstract class QueryResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    open val queryTask: DlQueryTask,

    @OneToOne(cascade = [CascadeType.ALL])
    open var computationStatistics: DlComputationStatistics?,

    open val queryResultStatus: QueryResultStatus,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0
}

interface QueryResultRepository : JpaRepository<QueryResult, Long> {
    fun findAllByQueryTask(queryTask: DlQueryTask): List<QueryResult>
}