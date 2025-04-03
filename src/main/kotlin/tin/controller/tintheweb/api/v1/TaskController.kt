package tin.controller.tintheweb.api.v1

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import tin.data.tintheweb.DLqueryTask.TaskConfigurationData
import tin.data.tintheweb.DLqueryTask.TaskInfoData
import tin.data.tintheweb.DLqueryTask.TaskQueueBody
import tin.model.v2.File.TinFile
import tin.model.v2.Tasks.*

import tin.services.Task.TaskService
import tin.services.files.FileService

@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val taskService: TaskService,
    private val fileService: FileService
) {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class ResourceNotFoundException(message: String) : RuntimeException(message)

    @GetMapping("/all")
    fun getTasks(): List<TaskInfoData> {
        val list = taskService.getTasks().toList()
        val infoList: MutableList<TaskInfoData> = mutableListOf();
        for (task in list) {
            val ontology = fileService.getFile(task.ontologyFile)
            val query = fileService.getFile(task.queryFile)
            var transducer: TinFile? = null
            if(task.transducerMode === TransducerMode.provided && task.transducerFile != null) {
                transducer = fileService.getFile(task.transducerFile)
            }
            infoList.add(TaskInfoData(task, ontology, query, transducer))
        }
        return infoList;
    }

    @GetMapping("/queue")
    fun getQueue(): List<TaskInfoData> {
        val list = taskService.getQueuedTasks().toList()
        val infoList: MutableList<TaskInfoData> = mutableListOf();
        for (task in list) {
            val ontology = fileService.getFile(task.ontologyFile)
            val query = fileService.getFile(task.queryFile)
            var transducer: TinFile? = null
            if(task.transducerMode === TransducerMode.provided && task.transducerFile != null) {
                transducer = fileService.getFile(task.transducerFile)
            }
            infoList.add(TaskInfoData(task, ontology, query, transducer))
        }
        return infoList;
    }

    @GetMapping("/{taskId}")
    fun getTask(@PathVariable taskId: Long, response: HttpServletResponse): Task? {
        val task = taskService.getTask(taskId);
        if(task == null) throw ResourceNotFoundException("Task with ID $taskId not found.");
        return task
    }

    @PostMapping("/add")
    fun addTask(@RequestBody data: TaskConfigurationData): Task {
        val fileConfiguration = TaskFileConfiguration(data.queryFileIdentifier, data.ontologyFileIdentifier, data.transducerMode, data.transducerGenerationMode, data.transducerFileIdentifier );
        val runtimeConfiguration = TaskRuntimeConfiguration(data.ontologyVariant)
        val computationConfiguration = TaskComputationConfiguration(data.computationMode, data.sourceIndividual, data.targetIndividual, data.maxCost)
        val task = taskService.createTask(fileConfiguration, runtimeConfiguration, computationConfiguration)
        taskService.addTask(task);
        return task;
    }

    @PostMapping("/queue")
    fun queueTask(@RequestBody taskQueueBody: TaskQueueBody): List<TaskInfoData> {
        taskService.queueTask(taskQueueBody.taskId);
        return getTasks();
    }

    @PostMapping("/unqueue")
    fun unqueueTask(@RequestBody taskQueueBody: TaskQueueBody): List<TaskInfoData> {
        taskService.removeFromQueue(taskQueueBody.taskId);
        return getTasks();
    }

    @GetMapping("/processNext")
    fun processNext(): String {
        val result = taskService.processNext();
        return result.name;
    }
}