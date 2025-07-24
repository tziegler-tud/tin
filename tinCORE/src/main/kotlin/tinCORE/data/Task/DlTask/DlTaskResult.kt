package tinCORE.data.Task.DlTask

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import tinCORE.data.Task.TaskResult
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.services.ResultGraph.ShortestPathResult

@Entity
class DlTaskResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    override val task: DlTask = DlTask(),

    override val source: String? = null,
    override val target: String? = null,
    override val sourceNode: String = "",
    override val targetNode: String = "",
    override val cost: Int? = null,
) : TaskResult {
    @GeneratedValue
    @Id
    override val id: Long = 0

    constructor(task: DlTask, shortestPathResult: ShortestPathResult<ResultNode>) : this(task,
        shortestPathResult.source.individual.identifier,
        shortestPathResult.target.individual.identifier,
        shortestPathResult.source.toString(),
        shortestPathResult.target.toString(),
        shortestPathResult.cost
    )
}

interface DlTaskResultRepository : JpaRepository<TaskResult, Long> {

}