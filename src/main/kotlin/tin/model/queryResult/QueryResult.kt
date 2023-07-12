package tin.model.queryResult

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.converter.AnswerSetConverter
import tin.model.queryTask.QueryTask
import javax.persistence.*

@Entity
class QueryResult(

    @OneToOne
    @JoinColumn(name = "query_task_id")
    val queryTask: QueryTask,

    @OneToOne(cascade = [CascadeType.ALL])
    val computationStatistics: ComputationStatistics?,

    val queryResultStatus: QueryResultStatus,

    @Convert(converter = AnswerSetConverter::class)
    val answerSet: Set<AnswerTriplet>

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
    @Embeddable
    data class AnswerTriplet(
        val source: String,
        val target: String,
        val cost: Double

    )

}

interface QueryResultRepository : JpaRepository<QueryResult, Long>