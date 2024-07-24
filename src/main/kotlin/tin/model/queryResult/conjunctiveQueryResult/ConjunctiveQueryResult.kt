package tin.model.queryResult.conjunctiveQueryResult

import tin.model.queryResult.QueryResult
import tin.model.queryResult.QueryResultStatus
import tin.model.queryResult.RegularPathQueryResult
import tin.model.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tin.model.queryTask.QueryTask
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany

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