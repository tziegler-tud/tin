package tin.services.Task

import tin.model.v2.Tasks.TaskComputationConfiguration
import tin.model.v2.Tasks.TaskFileConfiguration
import tin.model.v2.Tasks.TaskRuntimeConfiguration

class Task(
    private val taskFileConfiguration: TaskFileConfiguration,
    private val taskRuntimeConfiguration: TaskRuntimeConfiguration,
    private val taskComputationConfiguration: TaskComputationConfiguration,
) {
    private var state: TaskStatus = TaskStatus.Created

    fun setState(newState: TaskStatus) {
        state = newState;
    }

    fun getState(): TaskStatus {
        return state
    }

    fun getFileConfiguration(): TaskFileConfiguration {
        return taskFileConfiguration
    }

    fun getRuntimeConfiguration(): TaskRuntimeConfiguration {
        return taskRuntimeConfiguration
    }

    fun getComputationConfiguration(): TaskComputationConfiguration {
        return taskComputationConfiguration
    }

}