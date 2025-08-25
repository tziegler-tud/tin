package tinCORE.controller.v2

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import tinCORE.data.File.TinFile
import tinCORE.data.Task.DlTask.DlTaskComputationConfiguration
import tinCORE.data.Task.Task
import tinCORE.data.Task.TaskFileConfiguration
import tinCORE.data.Task.TaskInfoData
import tinCORE.data.Task.TransducerMode
import tinCORE.data.tintheweb.DLqueryTask.DlTaskQueueBody
import tinCORE.data.tintheweb.DlQueryTask.DlTaskConfigurationData
import tinCORE.services.File.FileService
import tinCORE.services.Task.DlTaskService

@RestController
@RequestMapping("/api/v2/dl/tasks")
class DlTaskController(
    private val taskService: DlTaskService,
    private val fileService: FileService
) {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class ResourceNotFoundException(message: String) : RuntimeException(message)

    @GetMapping("/all")
    fun getTasks(): List<TaskInfoData> {
        val list = taskService.getTasks().toList()
        val infoList: MutableList<TaskInfoData> = mutableListOf();
        for (task in list) {
            val ontology = fileService.getFile(task.dataFile)
            val query = fileService.getFile(task.queryFile)
            var transducer: TinFile? = null
            if(task.transducerMode === TransducerMode.provided && task.transducerFile != null) {
                transducer = fileService.getFile(task.transducerFile!!)
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
            val ontology = fileService.getFile(task.dataFile)
            val query = fileService.getFile(task.queryFile)
            var transducer: TinFile? = null
            if(task.transducerMode === TransducerMode.provided && task.transducerFile != null) {
                transducer = fileService.getFile(task.transducerFile!!)
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
    fun addTask(@RequestBody data: DlTaskConfigurationData): Task {
        val fileConfiguration = TaskFileConfiguration(
            data.queryFileIdentifier,
            data.ontologyFileIdentifier,
            data.transducerMode,
            data.transducerGenerationMode,
            data.transducerFileIdentifier
        );
        val computationConfiguration = DlTaskComputationConfiguration(data.ontologyVariant, data.computationMode, data.sourceIndividual, data.targetIndividual, data.maxCost)
        val task = taskService.createTask(fileConfiguration, computationConfiguration)
        taskService.addTask(task);
        return task;
    }

    @PostMapping("/queue")
    fun queueTask(@RequestBody taskQueueBody: DlTaskQueueBody): List<TaskInfoData> {
        taskService.queueTask(taskQueueBody.taskId);
        return getTasks();
    }

    @PostMapping("/unqueue")
    fun unqueueTask(@RequestBody taskQueueBody: DlTaskQueueBody): List<TaskInfoData> {
        taskService.removeFromQueue(taskQueueBody.taskId);
        return getTasks();
    }

    @GetMapping("/processNext")
    fun processNext(): String {
        val result = taskService.processNext();
        return result.name;
    }
}