package tin.controller.tintheweb.api.v1

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import tin.data.tintheweb.DLqueryTask.TaskConfigurationData
import tin.data.tintheweb.DLqueryTask.TaskInfoData

import tin.model.v2.Tasks.TaskComputationConfiguration
import tin.model.v2.Tasks.TaskFileConfiguration
import tin.model.v2.Tasks.TaskRuntimeConfiguration
import tin.services.Task.TaskService

@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class ResourceNotFoundException(message: String) : RuntimeException(message)

    @GetMapping("/all")
    fun getTasks(): List<TaskInfoData> {
        val list = taskService.getTasks().toList()
        val infoList: MutableList<TaskInfoData> = mutableListOf();
        for (task in list) {
            infoList.add(TaskInfoData(task))
        }
        return infoList;
    }

    @GetMapping("/{taskId}")
    fun getTasks(@PathVariable taskId: Long, response: HttpServletResponse): TaskInfoData? {
        val task = taskService.getTask(taskId);
        if(task == null) throw ResourceNotFoundException("Task with ID $taskId not found.");
        return TaskInfoData(task);
    }

    @PostMapping("/add")
    fun addTask(@RequestBody data: TaskConfigurationData): TaskInfoData {
        val fileConfiguration = TaskFileConfiguration(data.queryFileIdentifier, data.ontologyFileIdentifier, data.transducerMode, data.transducerGenerationMode, data.transducerFileIdentifier );
        val runtimeConfiguration = TaskRuntimeConfiguration(data.ontologyVariant)
        val computationConfiguration = TaskComputationConfiguration(data.computationMode, data.sourceIndividual, data.targetIndividual, data.maxCost)
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