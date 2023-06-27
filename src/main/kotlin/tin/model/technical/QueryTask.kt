package tin.model.technical

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.technical.internal.ComputationMode
import java.util.*
import javax.persistence.*

@Entity
class QueryTask(
    val queryFileIdentifier: Long,
    val transducerFileIdentifier: Long?,
    val databaseFileIdentifier: Long,
    var queryStatus: QueryStatus,

    @OneToMany(mappedBy = "queryTask")
    val queryResult: Set<QueryResult>?,

    @OneToOne
    val computationMode: ComputationMode,

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
}

interface QueryTaskRepository : JpaRepository<QueryTask, Long> {

    fun findFirstByOrderByCreatedAtAsc(): QueryTask?

}