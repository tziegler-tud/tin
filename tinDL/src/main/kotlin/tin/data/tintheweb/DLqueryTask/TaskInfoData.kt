package tin.data.tintheweb.DLqueryTask

import tin.model.v2.Tasks.Task

class TaskInfoData(val task: Task) {
    val id = task.id;
    val fileConfiguration = task.getFileConfiguration();
    val computationConfiguration = task.getComputationConfiguration();
    val runtimeConfiguration = task.getRuntimeConfiguration();
    val status = task.state
}

