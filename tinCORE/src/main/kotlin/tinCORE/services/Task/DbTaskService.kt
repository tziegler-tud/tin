package tinCORE.services.Task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tinCORE.data.Task.*
import tinCORE.data.Task.DbTask.Benchmark.DbBenchmarkResult
import tinCORE.data.Task.DbTask.Benchmark.DbBenchmarkResultsRepository
import tinCORE.data.Task.DbTask.Benchmark.DbTaskProcessingBenchmarkResult
import tinCORE.data.Task.DbTask.DbTask
import tinCORE.data.Task.DbTask.DbTaskComputationConfiguration
import tinCORE.data.Task.DbTask.DbTaskRepository
import tinCORE.data.Task.DbTask.DbTaskResult
import tinCORE.data.Task.DbTask.DbTaskResultRepository
import tinCORE.services.Task.TaskProcessor.TaskProcessorExecutionResult
import tinCORE.services.File.FileService
import tinCORE.services.Task.TaskProcessor.DbTaskProcessor
import tinCORE.services.internal.fileReaders.DatabaseReaderServiceV2
import tinCORE.services.technical.SystemConfigurationService
import tinCORE.services.internal.fileReaders.QueryReaderServiceV2
import tinCORE.services.internal.fileReaders.TransducerReaderServiceV2
import tinDB.model.v2.ResultGraph.DbResultNode

import tinLIB.model.v2.transducer.TransducerGraph

@Service
class DbTaskService @Autowired constructor(
    private val fileService: FileService,
    private val taskRepository: DbTaskRepository,
    private val taskResultRepository: DbTaskResultRepository,
    private val benchmarkResultRepository: DbBenchmarkResultsRepository,
    private val systemConfigurationService: SystemConfigurationService,
):  TaskService<DbTask> {

    override val taskQueue: TaskQueue = TaskQueue();
    override var isProcessing: Boolean = false;

    override val queryFileReader = QueryReaderServiceV2(systemConfigurationService);
    override val transducerFileReader = TransducerReaderServiceV2(systemConfigurationService);
    val databaseReader = DatabaseReaderServiceV2(systemConfigurationService);

    override fun createTask(taskFileConfiguration: TaskFileConfiguration, taskComputationConfiguration: TaskComputationConfiguration): DbTask {
        if(taskComputationConfiguration !is DbTaskComputationConfiguration) {
            throw Error("Failed to create Task: Invalid configuration given.")
        }
        else return createTask(taskFileConfiguration, taskComputationConfiguration);
    }

    fun createTask(taskFileConfiguration: TaskFileConfiguration, taskComputationConfiguration: DbTaskComputationConfiguration): DbTask {
        val task = DbTask(taskFileConfiguration, taskComputationConfiguration);
        taskRepository.save(task)
        return task;
    }

    /**
     * adds a task to the queue
     * @return boolean returns true if the queue was empty before the insertion
     */
    override fun addTask(entity: DbTask): Boolean {
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

    override fun getTasks() : List<DbTask> {
        return taskRepository.findAll();
    }

    override fun getTask(taskId: Long): DbTask? {
        return taskRepository.findById(taskId).orElse(null);
    }

    override fun getQueuedTasks() : List<DbTask> {
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

        val result: DbProcessingResult = processTask(task);

        task.state = TaskStatus.Finished;

        val list = result.results
        list.forEach {
            taskResultRepository.save(it)
        }
        val benchmarkResult = DbBenchmarkResult(task, result.benchmarkResult!!)
        benchmarkResultRepository.save(benchmarkResult);
        return result.processingResultStatus;
    }



    override fun processTask(task: DbTask) : DbProcessingResult {
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

        val databaseResult = databaseReader.processFile(df, false)
        val databaseGraph = databaseResult.get()
        val processor : DbTaskProcessor = DbTaskProcessor(task, queryResult.get(), fileConfiguration.transducerMode, fileConfiguration.transducerGenerationMode, transducerGraph, databaseGraph);

        val executionResult: TaskProcessorExecutionResult<DbResultNode, DbTaskProcessingBenchmarkResult> = processor.execute()
        val resultList: List<DbTaskResult> = executionResult.results.map { it ->
            DbTaskResult(
                task,
                source = it.source.individual.identifier,
                target = it.target.individual.identifier,
                sourceNode = it.source.toString(),
                targetNode = it.target.toString(),
                cost = it.cost
            )
        }
        //save results to DB?
        return DbProcessingResult(
            ProcessingResultStatus.SUCCESS,
            resultList,
            executionResult.benchmarkResult
        )
    }
}