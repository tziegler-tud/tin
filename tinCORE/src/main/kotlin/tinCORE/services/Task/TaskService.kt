package tinCORE.services.Task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tinCORE.data.Task.*
import tinCORE.data.Task.DlTask.Benchmark.BenchmarkResult
import tinCORE.data.Task.DlTask.Benchmark.BenchmarkResultsRepository
import tinCORE.services.Task.TaskProcessor.DlTaskProcessor
import tinCORE.services.Task.TaskProcessor.TaskProcessor
import tinCORE.services.Task.TaskProcessor.TaskProcessorExecutionResult
import tinCORE.services.File.FileService
import tinCORE.services.technical.SystemConfigurationService
import tinCORE.services.internal.fileReaders.QueryReaderServiceV2
import tinCORE.services.internal.fileReaders.TransducerReaderServiceV2
import tinCORE.services.internal.fileReaders.OntologyReaderService
import tinDL.model.v2.ResultGraph.DlResultNode

import tinDL.services.ontology.OntologyManager
import tinLIB.model.v2.transducer.TransducerGraph
import tinLIB.services.ResultGraph.ShortestPathResult

@Service
class TaskService @Autowired constructor(
    private val fileService: FileService,
    private val taskRepository: TaskRepository,
    private val benchmarkResultRepository: BenchmarkResultsRepository,
    private val taskResultRepository: TaskResultRepository,
    private val systemConfigurationService: SystemConfigurationService,
) {
    private val taskQueue: TaskQueue = TaskQueue();
    private var isProcessing: Boolean = false;

    private val queryFileReader = QueryReaderServiceV2(systemConfigurationService);
    private val transducerFileReader = TransducerReaderServiceV2(systemConfigurationService);
    private val ontologyReader = OntologyReaderService(systemConfigurationService);


    fun createTask(taskFileConfiguration: TaskFileConfiguration, taskRuntimeConfiguration: TaskRuntimeConfiguration, taskComputationConfiguration: TaskComputationConfiguration): Task {
        val task = Task(taskFileConfiguration, taskRuntimeConfiguration, taskComputationConfiguration);
        taskRepository.save(task)
        return task;
    }

    /**
     * adds a task to the queue
     * @return boolean returns true if the queue was empty before the insertion
     */
    fun addTask(entity: Task): Boolean {
        val isEmpty = taskQueue.isEmpty();
        taskQueue.add(entity);
        return isEmpty;
    }

    /**
     * adds a tasks to the queue. Returns true if the queue was empty before the operation
     */
    @Transactional
    fun queueTask(taskId: Long): Boolean {
        val entity = taskRepository.findById(taskId).orElse(null);
        if (entity == null) return false;
        entity.state = TaskStatus.Queued
        taskQueue.add(taskId);
        return true;
    }

    /**
     * removes a tasks from the queue. Returns true if the tasks was removed
     */
    @Transactional
    fun removeFromQueue(taskId: Long): Boolean {
        val entity = taskRepository.findById(taskId).orElse(null);
        if (entity == null) return false;
        if(entity.state !== TaskStatus.Queued) return false;
        entity.state = TaskStatus.Created
        taskQueue.remove(taskId)
        return true;
    }

    fun getTasks() : List<Task> {
        return taskRepository.findAll();
    }

    fun getTask(taskId: Long): Task? {
        return taskRepository.findById(taskId).orElse(null);
    }

    fun getQueuedTasks() : List<Task> {
        return taskRepository.findAllByState(TaskStatus.Queued);
    }

    @Transactional
    fun processNext() : ProcessingResultStatus {
        if(taskQueue.isEmpty()) return ProcessingResultStatus.EMPTY;
        if(isProcessing) return ProcessingResultStatus.BLOCKED;

        val id = taskQueue.getNext() ?: return ProcessingResultStatus.EMPTY
        val task = taskRepository.findById(id).orElse(null);
        if(task == null) return ProcessingResultStatus.FAILURE

        task.state = TaskStatus.Calculating;

        val result: ProcessingResult = processTask(task);

        task.state = TaskStatus.Finished;

        val list = result.results
        list.forEach {
            taskResultRepository.save(it)
        }
        val benchmarkResult = BenchmarkResult(task, result.benchmarkResult!!)
        benchmarkResultRepository.save(benchmarkResult);
        return result.processingResultStatus;
    }



    private fun processTask(task: Task) : ProcessingResult {
        isProcessing = true;
        val fileConfiguration = task.getFileConfiguration();

        val queryFile = fileService.getFile(fileConfiguration.queryFileIdentifier)!!;
        val dataFile = fileService.getFile(fileConfiguration.dataSourceFileIdentifier)!!;

        val qf = fileService.loadFileContent(queryFile);
        val df = fileService.loadFileContent(dataFile);

        //file readers
        //read and check for error
        val queryResult = queryFileReader.processFile(qf, false)




        var transducerGraph: TransducerGraph? = null;

        //if provided, read transducer file
        if(fileConfiguration.transducerMode == TransducerMode.provided) {
            if(fileConfiguration.transducerFileIdentifier != null) {
                val transducerFile = fileService.getFile(fileConfiguration.transducerFileIdentifier)!!;
                val tf = fileService.loadFileContent(transducerFile);
                val transducerResult = transducerFileReader.processFile(tf, false)
                transducerGraph = transducerResult.graph
            }
            else  {
                throw IllegalArgumentException("Expected provided transducer file, but no file identifier was given.");
            }
        }
        else {
            if(fileConfiguration.transducerGenerationMode == null) {
                throw IllegalArgumentException("Transducer Mode set to ${fileConfiguration.transducerMode.name}, but required argument TransducerGenerationMode is null.");

            }
        }

        when(task.taskType){
            TaskType.DBTask -> TODO()
            TaskType.DlTask -> {
                val ontologyResult = ontologyReader.processFile(df, false)
                val manager: OntologyManager = OntologyManager(ontologyResult.get())

                val processor : DlTaskProcessor = DlTaskProcessor(task, manager, queryResult.get(), fileConfiguration.transducerMode, fileConfiguration.transducerGenerationMode, transducerGraph);

                val executionResult: TaskProcessorExecutionResult<DlResultNode> = processor.execute()
                val resultList: List<TaskResult> = executionResult.results.map { it ->
                    TaskResult(
                        task,
                        source = it.source.individual.identifier,
                        target = it.target.individual.identifier,
                        sourceNode = it.source.toString(),
                        targetNode = it.target.toString(),
                        cost = it.cost
                    )
                }
                //save results to DB?
                return ProcessingResult(
                    ProcessingResultStatus.SUCCESS,
                    resultList,
                    executionResult.benchmarkResult
                )
            }
            TaskType.unset -> {
                throw IllegalArgumentException("Task Type ${task.taskType} not set.");
            }
        }


    }
}