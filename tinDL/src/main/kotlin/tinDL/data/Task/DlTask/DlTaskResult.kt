package tinDL.data.Task.DlTask
import jakarta.persistence.*
import org.semanticweb.owlapi.model.IRI
import org.springframework.data.jpa.repository.JpaRepository
import tinDL.model.v2.ResultGraph.DlResultNode
import tinLIB.services.ResultGraph.ShortestPathResult

@Entity
class DlTaskResult(
    @OneToOne(cascade = [CascadeType.ALL])
    val task: DlTask = DlTask(),

    val source: IRI? = null,
    val target: IRI? = null,
    val sourceNode: String = "",
    val targetNode: String = "",
    val cost: Int? = null,
) {
    @GeneratedValue
    @Id
    val id: Long = 0

    constructor(task: DlTask, shortestPathResult: ShortestPathResult<DlResultNode>) : this(task,
        shortestPathResult.source.individual.getIri(),
        shortestPathResult.target.individual.getIri(),
        shortestPathResult.source.toString(),
        shortestPathResult.target.toString(),
        shortestPathResult.cost
    )
}

interface TaskResultRepository : JpaRepository<DlTaskResult, Long> {

}