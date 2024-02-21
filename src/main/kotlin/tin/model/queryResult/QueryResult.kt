package tin.model.queryResult

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.queryTask.QueryTask
import javax.persistence.*

@Entity
abstract class QueryResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    open val queryTask: QueryTask,

    @OneToOne(cascade = [CascadeType.ALL])
    open var computationStatistics: ComputationStatistics?,

    open val queryResultStatus: QueryResultStatus,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0

    enum class QueryResultStatus {
        NoError,
        QueryFileNotFound,
        TransducerFileNotFound,
        DatabaseFileNotFound,
        ErrorInComputationMode,
        ErrorInComputationProperties
    }
}

interface QueryResultRepository : JpaRepository<QueryResult, Long> {
    fun findAllByQueryTask(queryTask: QueryTask): List<QueryResult>
}