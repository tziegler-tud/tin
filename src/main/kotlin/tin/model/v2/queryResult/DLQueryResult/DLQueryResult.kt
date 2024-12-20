package tin.model.v2.queryResult.DLQueryResult

import tin.model.v2.queryResult.QueryResult
import tin.model.v2.queryResult.QueryResultStatus
import tin.model.v2.queryResult.computationStatistics.DlRegularPathComputationStatistics
import tin.services.Task.Task
import javax.persistence.Entity

@Entity
class DLQueryResult(
    queryTask: Task,
    computationStatistics: DlRegularPathComputationStatistics,
    queryResultStatus: QueryResultStatus,
    ) : QueryResult(queryTask, computationStatistics, queryResultStatus)