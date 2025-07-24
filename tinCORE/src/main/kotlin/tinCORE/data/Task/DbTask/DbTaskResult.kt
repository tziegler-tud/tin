package tinCORE.data.Task.DbTask

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import tinCORE.data.Task.TaskResult
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.services.ResultGraph.ShortestPathResult

@Entity
class DlTaskResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    override val task: DbTask = DbTask(),

    override val source: String? = null,
    override val target: String? = null,
    override val sourceNode: String = "",
    override val targetNode: String = "",
    override val cost: Int? = null,
) : TaskResult {
    @GeneratedValue
    @Id
    override val id: Long = 0

    constructor(task: DbTask, shortestPathResult: ShortestPathResult<ResultNode>) : this(task,
        shortestPathResult.source.individual.identifier,
        shortestPathResult.target.individual.identifier,
        shortestPathResult.source.toString(),
        shortestPathResult.target.toString(),
        shortestPathResult.cost
    )
}

interface DbTaskResultRepository : JpaRepository<TaskResult, Long> {

}