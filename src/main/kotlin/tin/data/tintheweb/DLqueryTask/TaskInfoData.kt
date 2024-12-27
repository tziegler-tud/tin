package tin.data.tintheweb.DLqueryTask

import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLEntity
import tin.model.v2.Tasks.TaskFileConfiguration
import tin.model.v2.Tasks.TaskRuntimeConfiguration
import tin.services.Task.Task
import tin.services.Task.TaskStatus

class TaskInfoData(val task: Task) {
    val fileConfiguration = task.getFileConfiguration();
    val computationConfiguration = task.getComputationConfiguration();
    val runtimeConfiguration = task.getRuntimeConfiguration();
    val status = task.getState();
}

