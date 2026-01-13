package tinCORE.data.Task.DbTask

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import tinCORE.data.Task.*
import tinCORE.services.Task.TaskStatus

@Entity
class DbTask(
    queryFile: Long = 0,
    dataFile: Long = 0,

    taskType: TaskType = TaskType.unset,

    transducerMode: TransducerMode = TransducerMode.UNSET,
    transducerGenerationMode: TransducerGenerationMode? = null,
    transducerFile: Long? = null,

    individualNameA: String? = null,
    individualNameB: String? = null,
    maxCost: Int? = null,

    val computationMode: DbComputationMode = DbComputationMode.UNSET,


    ) : AbstractTask(
    queryFile,
    dataFile,
    taskType,
    transducerMode,
    transducerGenerationMode,
    transducerFile,
    individualNameA,
    individualNameB,
    maxCost
) {

    @GeneratedValue
    @Id
    override val id: Long = 0;

    override var state: TaskStatus = TaskStatus.Created;

    override fun getComputationConfiguration(): DbTaskComputationConfiguration {
        return DbTaskComputationConfiguration(computationMode, individualNameA, individualNameB, maxCost)
    }

    constructor(fileConfiguration: TaskFileConfiguration, computationConfiguration: DbTaskComputationConfiguration) : this(
        queryFile = fileConfiguration.queryFileIdentifier,
        dataFile = fileConfiguration.dataSourceFileIdentifier,
        transducerMode = fileConfiguration.transducerMode,
        transducerGenerationMode = fileConfiguration.transducerGenerationMode,
        transducerFile = fileConfiguration.transducerFileIdentifier,
        computationMode = computationConfiguration.computationMode,
        individualNameA = computationConfiguration.individualNameA,
        individualNameB = computationConfiguration.individualNameB,
        maxCost = computationConfiguration.maxCost,
    )
}

interface DbTaskRepository : JpaRepository<DbTask, Long> {
    fun findAllByState(state: TaskStatus): List<DbTask>
}