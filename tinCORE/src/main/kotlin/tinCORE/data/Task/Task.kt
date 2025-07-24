package tinCORE.data.Task

import tinCORE.services.Task.TaskStatus

interface Task {
    val id: Long

    val queryFile: Long
    val dataFile: Long

    val taskType: TaskType

    val transducerMode: TransducerMode
    val transducerGenerationMode: TransducerGenerationMode?
    val transducerFile: Long?

    val individualNameA: String?
    val individualNameB: String?
    val maxCost: Int?

    var state: TaskStatus

    fun getFileConfiguration(): TaskFileConfiguration

    fun getComputationConfiguration(): TaskComputationConfiguration
}