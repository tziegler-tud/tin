package tinCORE.services.Task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tinCORE.data.Task.*
import tinCORE.data.Task.DlTask.Benchmark.DlBenchmarkResult
import tinCORE.data.Task.DlTask.Benchmark.DlBenchmarkResultsRepository
import tinCORE.data.Task.DlTask.Benchmark.DlTaskProcessingBenchmarkResult
import tinCORE.data.Task.DlTask.DlTask
import tinCORE.data.Task.DlTask.DlTaskComputationConfiguration
import tinCORE.data.Task.DlTask.DlTaskRepository
import tinCORE.data.Task.DlTask.DlTaskResult
import tinCORE.data.Task.DlTask.DlTaskResultRepository
import tinCORE.services.Task.TaskProcessor.DlTaskProcessor
import tinCORE.services.Task.TaskProcessor.TaskProcessorExecutionResult
import tinCORE.services.File.FileService
import tinCORE.services.technical.SystemConfigurationService
import tinCORE.services.internal.fileReaders.QueryReaderServiceV2
import tinCORE.services.internal.fileReaders.TransducerReaderServiceV2
import tinCORE.services.internal.fileReaders.OntologyReaderService
import tinDL.model.v2.ResultGraph.DlResultNode

import tinDL.services.ontology.OntologyManager
import tinLIB.model.v2.transducer.TransducerGraph

@Service
class DlTaskService @Autowired constructor(
    private val fileService: FileService,
    private val taskRepository: DlTaskRepository,
    private val taskResultRepository: DlTaskResultRepository,
    private val benchmarkResultRepository: DlBenchmarkResultsRepository,
    private val systemConfigurationService: SystemConfigurationService,
):  TaskService<DlTask> {

    override val taskQueue: TaskQueue = TaskQueue();
    override var isProcessing: Boolean = false;

    override val queryFileReader = QueryReaderServiceV2(systemConfigurationService);
    override val transducerFileReader = TransducerReaderServiceV2(systemConfigurationService);
    val ontologyReader = OntologyReaderService(systemConfigurationService);

    override fun createTask(taskFileConfiguration: TaskFileConfiguration, taskComputationConfiguration: TaskComputationConfiguration): DlTask {
        if(taskComputationConfiguration !is DlTaskComputationConfiguration) {
            throw Error("Failed to create Task: Invalid configuration given.")
        }
        else return createTask(taskFileConfiguration, taskComputationConfiguration);
    }

    fun createTask(taskFileConfiguration: TaskFileConfiguration, taskComputationConfiguration: DlTaskComputationConfiguration): DlTask {
        val task = DlTask(taskFileConfiguration, taskComputationConfiguration);
        taskRepository.save(task)
        return task;
    }

    /**
     * adds a task to the queue
     * @return boolean returns true if the queue was empty before the insertion
     */
    override fun addTask(entity: DlTask): Boolean {
        val isEmpty = taskQueue.isEmpty();
        taskQueue.add(entity);
        return isEmpty;
    }

    /**
     * adds a tasks to the queue. Returns true if the queue was empty before the operation
     */
    @Transactional
    override fun queueTask(taskId: Long): Boolean {
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
    override fun removeFromQueue(taskId: Long): Boolean {
        val entity = taskRepository.findById(taskId).orElse(null);
        if (entity == null) return false;
        if(entity.state !== TaskStatus.Queued) return false;
        entity.state = TaskStatus.Created
        taskQueue.remove(taskId)
        return true;
    }

    override fun getTasks() : List<DlTask> {
        return taskRepository.findAll();
    }

    override fun getTask(taskId: Long): DlTask? {
        return taskRepository.findById(taskId).orElse(null);
    }

    override fun getQueuedTasks() : List<DlTask> {
        return taskRepository.findAllByState(TaskStatus.Queued);
    }

    @Transactional
    override fun processNext() : ProcessingResultStatus {
        if(taskQueue.isEmpty()) return ProcessingResultStatus.EMPTY;
        if(isProcessing) return ProcessingResultStatus.BLOCKED;

        val id = taskQueue.getNext() ?: return ProcessingResultStatus.EMPTY
        val task = taskRepository.findById(id).orElse(null);
        if(task == null) return ProcessingResultStatus.FAILURE

        task.state = TaskStatus.Calculating;

        val result: DlProcessingResult = processTask(task);

        task.state = TaskStatus.Finished;

        val list = result.results
        list.forEach {
            taskResultRepository.save(it)
        }
        val benchmarkResult = DlBenchmarkResult(task, result.benchmarkResult!!)
        benchmarkResultRepository.save(benchmarkResult);
        return result.processingResultStatus;
    }



    override fun processTask(task: DlTask) : DlProcessingResult {
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

        val ontologyResult = ontologyReader.processFile(df, false)
        val manager: OntologyManager = OntologyManager(ontologyResult.get())

        val processor : DlTaskProcessor = DlTaskProcessor(task, manager, queryResult.get(), fileConfiguration.transducerMode, fileConfiguration.transducerGenerationMode, transducerGraph);

        val executionResult: TaskProcessorExecutionResult<DlResultNode, DlTaskProcessingBenchmarkResult> = processor.execute()
        val resultList: List<DlTaskResult> = executionResult.results.map { it ->
            DlTaskResult(
                task,
                source = it.source.individual.identifier,
                target = it.target.individual.identifier,
                sourceNode = it.source.toString(),
                targetNode = it.target.toString(),
                cost = it.cost
            )
        }
        //save results to DB?
        return DlProcessingResult(
            ProcessingResultStatus.SUCCESS,
            resultList,
            executionResult.benchmarkResult
        )
    }
}