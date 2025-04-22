package tinLIB.data.Task

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import tinLIB.services.Task.TaskStatus

@Entity
class Task(
    val taskType: TaskType = TaskType.unset,
    val queryFile: Long = 0,
    val transducerMode: TransducerMode = TransducerMode.UNSET,
    val transducerGenerationMode: TransducerGenerationMode? = null,
    val transducerFile: Long? = null,

){

    @GeneratedValue
    @Id
    val id: Long = 0;

    var state: TaskStatus = TaskStatus.Created;

    constructor(taskType: TaskType, fileConfiguration: TaskFileConfiguration) : this(
        taskType = taskType,
        queryFile = fileConfiguration.queryFileIdentifier,
        transducerMode = fileConfiguration.transducerMode,
        transducerGenerationMode = fileConfiguration.transducerGenerationMode,
        transducerFile = fileConfiguration.transducerFileIdentifier,
    )

    fun getType(): TaskType {
        return taskType;
    }

    fun getFileConfiguration(): TaskFileConfiguration {
        return TaskFileConfiguration(queryFile, transducerMode, transducerGenerationMode, transducerFile)
    }
}

interface TaskRepository : JpaRepository<Task, Long> {
    fun findAllByState(state: TaskStatus): List<Task>
}