package tin.model.queryResult.DLQueryResult

import tin.model.queryResult.QueryResult
import tin.model.queryResult.QueryResultStatus
import tin.model.queryResult.computationStatistics.ConjunctiveComputationStatistics
import tin.model.queryTask.QueryTask
import javax.persistence.Entity

@Entity
class DLQueryResult(
    queryTask: QueryTask,
    computationStatistics: ConjunctiveComputationStatistics?,
    queryResultStatus: QueryResultStatus,
    ) : QueryResult(queryTask, computationStatistics, queryResultStatus)