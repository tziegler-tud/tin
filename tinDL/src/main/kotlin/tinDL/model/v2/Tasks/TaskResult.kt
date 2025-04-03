package tinDL.model.v2.Tasks
import jakarta.persistence.*
import org.semanticweb.owlapi.model.IRI
import org.springframework.data.jpa.repository.JpaRepository
import tinDL.services.ontology.ResultGraph.ShortestPathResult

@Entity
class TaskResult(
    @OneToOne(cascade = [CascadeType.ALL])
    val task: Task = Task(),

    val source: IRI? = null,
    val target: IRI? = null,
    val sourceNode: String = "",
    val targetNode: String = "",
    val cost: Int? = null,
) {
    @GeneratedValue
    @Id
    val id: Long = 0

    constructor(task: Task, shortestPathResult: ShortestPathResult) : this(task,
        shortestPathResult.source.getIndividual().iri,
        shortestPathResult.target.getIndividual().iri,
        shortestPathResult.source.toString(),
        shortestPathResult.target.toString(),
        shortestPathResult.cost
    )
}

interface TaskResultRepository : JpaRepository<TaskResult, Long> {

}