package tinCORE.data.tinDB.queryResult

import jakarta.persistence.*
import tinCORE.data.Task.DbTask.DbTask
import tinCORE.data.tinDB.converter.AnswerSetConverter
import tinCORE.data.tinDB.queryResult.computationStatistics.ComputationStatistics
import tinCORE.data.tinDB.queryResult.conjunctiveQueryResult.ConjunctiveQueryResult

@Entity
class RegularPathQueryResult(
    queryTask: DbTask? = null,
    computationStatistics: ComputationStatistics? = null,
    queryResultStatus: QueryResultStatus? = null,
    val identifier: String? = null,

    @Convert(converter = AnswerSetConverter::class)
    @Column(columnDefinition = "longtext")
    val answerSet: Set<AnswerTriplet> = emptySet(),

    @ManyToOne
    @JoinColumn(name = "conjunctive_query_result_id")
    val conjunctiveQueryResult: ConjunctiveQueryResult? = null,

    ) : QueryResult(queryTask, computationStatistics, queryResultStatus) {

    @Embeddable
    data class AnswerTriplet(
        val source: String? = null,
        val target: String? = null,
        val cost: Double? = null,
    )
}
