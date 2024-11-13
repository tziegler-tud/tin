package tin.model.v1.queryResult.DLQueryResult

import tin.model.v1.queryResult.QueryResult
import tin.model.v1.queryResult.QueryResultStatus
import tin.model.v1.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tin.model.v1.queryTask.QueryTask
import javax.persistence.Entity

@Entity
class DLQueryResult(
    queryTask: QueryTask,
    computationStatistics: ConjunctiveComputationStatistics?,
    queryResultStatus: QueryResultStatus,
    ) : QueryResult(queryTask, computationStatistics, queryResultStatus)