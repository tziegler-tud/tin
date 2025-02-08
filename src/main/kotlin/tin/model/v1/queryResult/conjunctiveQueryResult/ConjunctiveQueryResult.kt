package tin.model.v1.queryResult.conjunctiveQueryResult

import tin.model.v1.queryResult.QueryResult
import tin.model.v1.queryResult.QueryResultStatus
import tin.model.v1.queryResult.RegularPathQueryResult
import tin.model.v1.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tin.model.v1.queryTask.QueryTask
import jakarta.persistence.*


@Entity
class ConjunctiveQueryResult(
    queryTask: QueryTask,
    computationStatistics: ConjunctiveComputationStatistics?,
    queryResultStatus: QueryResultStatus,

    @OneToMany(mappedBy = "conjunctiveQueryResult", cascade = [CascadeType.ALL])
    var variableMappings: Set<ConjunctiveQueryAnswerMapping>,

    @OneToMany(mappedBy = "conjunctiveQueryResult")
    val regularPathQueryResults: Set<RegularPathQueryResult>

) : QueryResult(queryTask, computationStatistics, queryResultStatus)