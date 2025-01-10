package tin.controller.tintheweb.api.v1

import org.apache.tomcat.jni.Proc
import org.springframework.web.bind.annotation.*
import tin.data.tintheweb.DLqueryTask.TaskConfigurationData
import tin.data.tintheweb.DLqueryTask.TaskInfoData
import tin.data.tintheweb.FileData
import tin.model.v2.Tasks.ComputationMode
import tin.model.v2.Tasks.TaskComputationConfiguration
import tin.model.v2.Tasks.TaskFileConfiguration
import tin.model.v2.Tasks.TaskRuntimeConfiguration
import tin.services.Task.ProcessingResult
import tin.services.Task.TaskService
import tin.services.tintheweb.FileOverviewService

@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping("/all")
    fun getTasks(): List<TaskInfoData> {
        val list = taskService.getTasks().toList()
        val infoList: MutableList<TaskInfoData> = mutableListOf();
        for (task in list) {
            infoList.add(TaskInfoData(task))
        }
        return infoList;
    }

    @PostMapping("/add")
    fun addTask(@RequestBody data: TaskConfigurationData): TaskInfoData {
//        if(data.queryFileIdentifier == null || data.transducerFileIdentifier == null || data.ontologyFileIdentifier == null) {
//
//        }
        val fileConfiguration = TaskFileConfiguration(data.queryFileIdentifier!!, data.transducerFileIdentifier!!, data.ontologyFileIdentifier!!);
        val runtimeConfiguration = TaskRuntimeConfiguration(data.ontologyVariant)
        val computationConfiguration = TaskComputationConfiguration(ComputationMode.allIndivudals, null, null, null)
        val task = taskService.createTask(fileConfiguration, runtimeConfiguration, computationConfiguration)
        taskService.addTask(task);
        return TaskInfoData(task);
    }

    @PostMapping("/queue")
    fun queueTasks(@RequestBody data: Long): List<TaskInfoData> {
        taskService.queueTask(data);
        return getTasks();
    }

    @GetMapping("/processNext")
    fun processNext(): String {
        val result = taskService.processNext();
        return result.name;
    }
}