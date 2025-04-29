package tinCORE.data.Task

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import tinLIB.model.v2.ResultGraph.ResultNode
import tinLIB.services.ResultGraph.ShortestPathResult

@Entity
class TaskResult(
    @ManyToOne(cascade = [CascadeType.ALL])
    val task: Task = Task(),

    val source: String? = null,
    val target: String? = null,
    val sourceNode: String = "",
    val targetNode: String = "",
    val cost: Int? = null,
) {
    @GeneratedValue
    @Id
    val id: Long = 0

    constructor(task: Task, shortestPathResult: ShortestPathResult<ResultNode>) : this(task,
        shortestPathResult.source.individual.identifier,
        shortestPathResult.target.individual.identifier,
        shortestPathResult.source.toString(),
        shortestPathResult.target.toString(),
        shortestPathResult.cost
    )
}

interface TaskResultRepository : JpaRepository<TaskResult, Long> {

}