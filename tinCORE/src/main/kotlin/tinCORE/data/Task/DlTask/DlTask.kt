package tinCORE.data.Task.DlTask

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import tinCORE.data.Task.*
import tinCORE.services.Task.TaskStatus

@Entity
class DlTask(
    queryFile: Long = 0,
    dataFile: Long = 0,

    taskType: TaskType = TaskType.unset,

    transducerMode: TransducerMode = TransducerMode.UNSET,
    transducerGenerationMode: TransducerGenerationMode? = null,
    transducerFile: Long? = null,

    individualNameA: String? = null,
    individualNameB: String? = null,
    maxCost: Int? = null,

    val ontologyVariant: OntologyVariant = OntologyVariant.UNSET,
    val computationMode: DlComputationMode = DlComputationMode.UNSET,


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

    constructor(fileConfiguration: TaskFileConfiguration, computationConfiguration: DlTaskComputationConfiguration) : this(
        queryFile = fileConfiguration.queryFileIdentifier,
        dataFile = fileConfiguration.dataSourceFileIdentifier,
        transducerMode = fileConfiguration.transducerMode,
        transducerGenerationMode = fileConfiguration.transducerGenerationMode,
        transducerFile = fileConfiguration.transducerFileIdentifier,
        ontologyVariant = computationConfiguration.ontologyVariant,
        computationMode = computationConfiguration.computationMode,
        individualNameA = computationConfiguration.individualNameA,
        individualNameB = computationConfiguration.individualNameB,
        maxCost = computationConfiguration.maxCost,
    )

    override fun getComputationConfiguration(): DlTaskComputationConfiguration {
        return DlTaskComputationConfiguration(ontologyVariant, computationMode, individualNameA, individualNameB, maxCost)
    }
}

interface DlTaskRepository : JpaRepository<DlTask, Long> {
    fun findAllByState(state: TaskStatus): List<DlTask>
}