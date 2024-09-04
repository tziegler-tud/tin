package tin.model.v1.queryTask

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.v1.queryResult.QueryResult
import java.util.*
import javax.persistence.*

@Entity
class QueryTask(
    val queryFileIdentifier: Long,
    val transducerFileIdentifier: Long?,
    val dataSourceFileIdentifier: Long,
    var queryStatus: QueryStatus,
    val queryType: QueryType,

    @OneToMany(mappedBy = "queryTask")
    val queryResult: List<QueryResult>? = listOf(),

    @ManyToOne(cascade = [CascadeType.ALL])
    val computationProperties: ComputationProperties

    ) {
    @GeneratedValue
    @Id
    val id: Long = 0

    val createdAt: Date = Date()

    enum class QueryStatus {
        Queued,
        Calculating,
        Finished,
        Error
    }

    enum class QueryType {
        regularPathQuery,
        conjunctiveQuery,
        DLQuery,
    }
}

interface QueryTaskRepository : JpaRepository<QueryTask, Long> {

    fun findFirstByQueryStatusOrderByCreatedAtAsc(queryStatus: QueryTask.QueryStatus): QueryTask?

}