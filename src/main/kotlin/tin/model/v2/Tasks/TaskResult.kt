package tin.model.v2.Tasks

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.v1.queryResult.computationStatistics.ComputationStatistics
import tin.model.v1.queryTask.QueryTask
import tin.services.Task.Task
import javax.persistence.*

@Entity
abstract class TaskResult(
    @OneToOne(cascade = [CascadeType.ALL])
    open val task: TaskEntity,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0
}

interface TaskResultRepository : JpaRepository<TaskResult, Long>