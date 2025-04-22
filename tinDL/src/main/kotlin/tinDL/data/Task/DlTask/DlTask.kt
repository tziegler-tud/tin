package tinDL.data.Task.DlTask

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import tinLIB.services.Task.TaskStatus

@Entity
class DlTask(
    val queryFile: Long = 0,
    val ontologyFile: Long = 0,

    val transducerMode: TransducerMode = TransducerMode.UNSET,
    val transducerGenerationMode: TransducerGenerationMode? = null,
    val transducerFile: Long? = null,

    val ontologyVariant: OntologyVariant = OntologyVariant.UNSET,
    val computationMode: ComputationMode = ComputationMode.UNSET,
    val individualNameA: String? = null,
    val individualNameB: String? = null,
    val maxCost: Int? = null,
){

    @GeneratedValue
    @Id
    val id: Long = 0;

    var state: TaskStatus = TaskStatus.Created;

    constructor(fileConfiguration: DlTaskFileConfiguration, runtimeConfiguration: DlTaskRuntimeConfiguration, computationConfiguration: DlTaskComputationConfiguration) : this(
        queryFile = fileConfiguration.queryFileIdentifier,
        ontologyFile = fileConfiguration.ontologyFileIdentifier,
        transducerMode = fileConfiguration.transducerMode,
        transducerGenerationMode = fileConfiguration.transducerGenerationMode,
        transducerFile = fileConfiguration.transducerFileIdentifier,
        ontologyVariant = runtimeConfiguration.ontologyVariant,
        computationMode = computationConfiguration.computationMode,
        individualNameA = computationConfiguration.individualNameA,
        individualNameB = computationConfiguration.individualNameB,
        maxCost = computationConfiguration.maxCost,
    )



    fun getFileConfiguration(): DlTaskFileConfiguration {
        return DlTaskFileConfiguration(queryFile, ontologyFile, transducerMode, transducerGenerationMode, transducerFile)
    }

    fun getRuntimeConfiguration(): DlTaskRuntimeConfiguration {
        return DlTaskRuntimeConfiguration(ontologyVariant)
    }

    fun getComputationConfiguration(): DlTaskComputationConfiguration {
        return DlTaskComputationConfiguration(computationMode, individualNameA, individualNameB, maxCost)
    }
}

interface TaskRepository : JpaRepository<DlTask, Long> {
    fun findAllByState(state: TaskStatus): List<DlTask>
}