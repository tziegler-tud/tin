package tin.model.v2.Tasks

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.v1.queryResult.computationStatistics.ComputationStatistics
import tin.model.v1.queryTask.QueryTask
import javax.persistence.*

@Entity
abstract class TaskResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    open val queryTask: QueryTask,

    @OneToOne(cascade = [CascadeType.ALL])
    open var computationStatistics: ComputationStatistics?,

    open val queryResultStatus: QueryResultStatus,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0
}

interface TaskResultRepository : JpaRepository<TaskResult, Long> {
    fun findAllByQueryTask(queryTask: QueryTask): List<TaskResult>
}