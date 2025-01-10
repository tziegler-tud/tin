package tin.model.v2.Tasks

import org.springframework.data.jpa.repository.JpaRepository
import tin.services.Task.TaskStatus
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Task(
    val queryFile: Long,
    val transducerFile: Long,
    val ontologyFile: Long,
    val ontologyVariant: OntologyVariant,

    val computationMode: ComputationMode,
    val individualNameA: String?,
    val individualNameB: String?,
    val maxCost: Int?,
){

    @GeneratedValue
    @Id
    val id: Long = 0;

    var state: TaskStatus = TaskStatus.Created;

    constructor(fileConfiguration: TaskFileConfiguration, runtimeConfiguration: TaskRuntimeConfiguration, computationConfiguration: TaskComputationConfiguration) : this(
        fileConfiguration.queryFileIdentifier,
        fileConfiguration.transducerFileIdentifier,
        fileConfiguration.ontologyFileIdentifier,
        runtimeConfiguration.ontologyVariant,
        computationConfiguration.computationMode,
        computationConfiguration.individualNameA,
        computationConfiguration.individualNameB,
        computationConfiguration.maxCost,
    )

    fun getFileConfiguration(): TaskFileConfiguration {
        return TaskFileConfiguration(queryFile, transducerFile, ontologyFile)
    }

    fun getRuntimeConfiguration(): TaskRuntimeConfiguration {
        return TaskRuntimeConfiguration(ontologyVariant)
    }

    fun getComputationConfiguration(): TaskComputationConfiguration {
        return TaskComputationConfiguration(computationMode, individualNameA, individualNameB, maxCost)
    }
}

interface TaskRepository : JpaRepository<Task, Long> {
    fun findAllByState(state: TaskStatus): List<Task>
}