package tinDB.model.v1.queryResult.conjunctiveQueryResult

import tinDB.model.v1.queryResult.QueryResult
import tinDB.model.v1.queryResult.QueryResultStatus
import tinDB.model.v1.queryResult.RegularPathQueryResult
import tinDB.model.v1.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tinDB.model.v1.queryTask.QueryTask
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