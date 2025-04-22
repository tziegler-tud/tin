package tinLIB.data.Task
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
class TaskResult(
    @OneToOne(cascade = [CascadeType.ALL])
    val task: Task = Task(),
    val taskType: TaskType = TaskType.unset,


    ) {
    @GeneratedValue
    @Id
    val id: Long = 0

    constructor() : this(
        taskType = TaskType.unset,
        task = Task()
    )
}

interface TaskResultRepository : JpaRepository<TaskResult, Long> {

}