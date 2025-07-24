package tinCORE.data.Task

import tinCORE.services.Task.TaskStatus

abstract class AbstractTask(
    override val queryFile: Long = 0,
    override val dataFile: Long = 0,

    override val taskType: TaskType = TaskType.unset,

    override val transducerMode: TransducerMode = TransducerMode.UNSET,
    override val transducerGenerationMode: TransducerGenerationMode? = null,
    override val transducerFile: Long? = null,

    override val individualNameA: String? = null,
    override val individualNameB: String? = null,
    override val maxCost: Int? = null,
) : Task {
    override val id: Long = 0;

    override var state: TaskStatus = TaskStatus.Created;

    override fun getFileConfiguration(): TaskFileConfiguration {
        return TaskFileConfiguration(queryFile, dataFile, transducerMode, transducerGenerationMode, transducerFile)
    }
}