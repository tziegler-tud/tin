package tinDL.model.v1.queryResult

import tinDL.model.v1.converter.AnswerSetConverter
import tinDL.model.v1.queryResult.computationStatistics.ComputationStatistics
import tinDL.model.v1.queryResult.conjunctiveQueryResult.ConjunctiveQueryResult
import tinDL.model.v1.queryTask.QueryTask
import jakarta.persistence.*

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
