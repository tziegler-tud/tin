package tin.model.queryResult

import tin.model.queryTask.QueryTask
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class ConjunctiveQueryResult(
    queryTask: QueryTask,
    computationStatistics: ComputationStatistics?,
    queryResultStatus: QueryResultStatus,

    @OneToMany(mappedBy = "conjunctiveQueryResult", cascade = [CascadeType.ALL])
    var variableMappings: Set<ConjunctiveQueryAnswerMapping>,

    @OneToMany(mappedBy = "conjunctiveQueryResult")
    val regularPathQueryResults: Set<RegularPathQueryResult>

) : QueryResult(queryTask, computationStatistics, queryResultStatus)