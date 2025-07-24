package tinCORE.data.tinDB.queryResult.conjunctiveQueryResult

import jakarta.persistence.*
import tinCORE.data.Task.DbTask.DbTask
import tinCORE.data.tinDB.queryResult.QueryResult
import tinCORE.data.tinDB.queryResult.QueryResultStatus
import tinCORE.data.tinDB.queryResult.RegularPathQueryResult
import tinCORE.data.tinDB.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tinDB.model.v1.queryResult.conjunctiveQueryResult.ConjunctiveQueryAnswerMapping


@Entity
class ConjunctiveQueryResult(
    queryTask: DbTask,
    computationStatistics: ConjunctiveComputationStatistics?,
    queryResultStatus: QueryResultStatus,

    @OneToMany(mappedBy = "conjunctiveQueryResult", cascade = [CascadeType.ALL])
    var variableMappings: Set<ConjunctiveQueryAnswerMapping>,

    @OneToMany(mappedBy = "conjunctiveQueryResult")
    val regularPathQueryResults: Set<RegularPathQueryResult>

) : QueryResult(queryTask, computationStatistics, queryResultStatus)