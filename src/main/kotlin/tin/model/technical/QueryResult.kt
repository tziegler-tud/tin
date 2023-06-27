package tin.model.technical

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.technical.internal.ComputationStatistics
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
class QueryResult(

    @OneToOne
    @JoinColumn(name = "query_task_id")
    val queryTask: QueryTask,

    @OneToOne
    val computationStatistics: ComputationStatistics?,

    val queryResultStatus: QueryResultStatus,

    @ElementCollection
    val answerMap: Map<String, Double>

) {
    @GeneratedValue
    @Id
    val id: Long = 0

    enum class QueryResultStatus {
        NoError,
        QueryFileNotFound,
        TransducerFileNotFound,
        DatabaseFileNotFound,
        ErrorInComputationMode,
        ErrorInComputationProperties
    }
}

interface QueryResultRepository : JpaRepository<QueryResult, Long>