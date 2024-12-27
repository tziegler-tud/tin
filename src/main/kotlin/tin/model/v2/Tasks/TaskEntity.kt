package tin.model.v2.Tasks

import org.springframework.data.jpa.repository.JpaRepository
import tin.services.Task.Task
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class TaskEntity(
    val queryFile: Long,
    val transducerFile: Long,
    val ontologyFile: Long,
    val ontologyVariant: OntologyVariant,
){

    @GeneratedValue
    @Id
    val id: Long = 0


    constructor(task: Task) : this(
        task.getFileConfiguration().queryFileIdentifier,
        task.getFileConfiguration().transducerFileIdentifier,
        task.getFileConfiguration().ontologyFileIdentifier,
        task.getRuntimeConfiguration().ontologyVariant
    )
}

interface TaskRepository : JpaRepository<TaskEntity, Long> {
//    fun findAllByQueryTask(task: TaskEntity): List<QueryResult>
}