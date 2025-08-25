package tinCORE.services.Task

import tinCORE.data.Task.*
import tinCORE.services.internal.fileReaders.QueryReaderServiceV2
import tinCORE.services.internal.fileReaders.TransducerReaderServiceV2
import tinCORE.services.internal.fileReaders.OntologyReaderService


interface TaskService<T: Task> {
    val taskQueue: TaskQueue
    var isProcessing: Boolean

    val queryFileReader: QueryReaderServiceV2
    val transducerFileReader: TransducerReaderServiceV2

    fun createTask(
        taskFileConfiguration: TaskFileConfiguration,
        taskComputationConfiguration: TaskComputationConfiguration
    ): T

    /**
     * adds a task to the queue
     * @return boolean returns true if the queue was empty before the insertion
     */
    fun addTask(entity: T): Boolean

    /**
     * adds a tasks to the queue. Returns true if the queue was empty before the operation
     */
    fun queueTask(taskId: Long): Boolean

    /**
     * removes a tasks from the queue. Returns true if the tasks was removed
     */
    fun removeFromQueue(taskId: Long): Boolean

    fun getTasks(): List<T>

    fun getTask(taskId: Long): T?

    fun getQueuedTasks(): List<T>

    fun processNext(): ProcessingResultStatus

    fun processTask(task: T): ProcessingResult
}