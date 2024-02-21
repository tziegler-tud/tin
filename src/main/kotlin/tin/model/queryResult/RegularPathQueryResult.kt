package tin.model.queryResult

import tin.model.converter.AnswerSetConverter
import tin.model.queryTask.QueryTask
import javax.persistence.*

@Entity
class RegularPathQueryResult(
    queryTask: QueryTask,
    computationStatistics: ComputationStatistics?,
    queryResultStatus: QueryResultStatus,
    val identifier: String?,

    @Convert(converter = AnswerSetConverter::class)
    @Column(columnDefinition = "longtext")
    val answerSet: Set<AnswerTriplet>,

    @ManyToOne
    @JoinColumn(name = "conjunctive_query_result_id")
    val conjunctiveQueryResult: ConjunctiveQueryResult? = null,

) : QueryResult(queryTask, computationStatistics, queryResultStatus) {

    @Embeddable
    data class AnswerTriplet(
        val source: String,
        val target: String,
        val cost: Double
    )

}
