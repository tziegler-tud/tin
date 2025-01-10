package tin.services.Task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.model.v2.Tasks.*
import tin.services.Task.Benchmark.TaskProcessingBenchmarkResult
import tin.services.files.FileService
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.QueryReaderServiceV2
import tin.services.internal.fileReaders.TransducerReaderServiceV2
import tin.services.ontology.OntologyManager
import tin.services.ontology.ResultGraph.ShortestPathResult
import tin.services.technical.SystemConfigurationService

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

    fun getTasks() : List<Task> {
        return taskRepository.findAll();
    }

    fun getQueuedTasks() : List<Task> {
        return taskRepository.findAllByState(TaskStatus.Queued);
    }

    @Transactional
    fun processNext() : ProcessingResultStatus {
        if(taskQueue.isEmpty()) return ProcessingResultStatus.EMPTY;
        if(isProcessing) return ProcessingResultStatus.BLOCKED;
        val id = taskQueue.getNext()
        val task = taskRepository.findById(id).orElse(null);
        if(task == null) return ProcessingResultStatus.FAILURE
        task.state = TaskStatus.Calculating;
        val result = processTask(task);
        task.state = TaskStatus.Finished;

        val list = result.result;
        list.forEach {
            val r = TaskResult(task, it)
            taskResultRepository.save(r)
        }
        val benchmarkResult = BenchmarkResult(task, result.benchmarkResult)
        benchmarkResultRepository.save(benchmarkResult);
        return result.processingResultStatus;
    }



    private fun processTask(task: Task) : ProcessingResult {
        isProcessing = true;
        val fileConfiguration = task.getFileConfiguration();
        //read query file
        val queryFile = fileService.getFile(fileConfiguration.queryFileIdentifier)!!;
        val transducerFile = fileService.getFile(fileConfiguration.transducerFileIdentifier)!!;
        val ontologyFile = fileService.getFile(fileConfiguration.ontologyFileIdentifier)!!;

        val qf = fileService.loadFileContent(queryFile);
        val tf = fileService.loadFileContent(transducerFile);
        val of = fileService.loadFileContent(ontologyFile);

        //file readers
        //read and check for error
        val queryResult = queryFileReader.processFile(qf, false)
        val transducerResult = transducerFileReader.processFile(tf, false)
        val ontologyResult = ontologyReader.processFile(of, false)

        val manager: OntologyManager = OntologyManager(ontologyResult.get())

        val processor = TaskProcessor(task, queryResult.get(), transducerResult.get(), manager);
        val resultPair: Pair<List<ShortestPathResult>, TaskProcessingBenchmarkResult> = processor.execute();
        isProcessing = false;
        //save results to DB?
        return ProcessingResult(ProcessingResultStatus.SUCCESS, resultPair.first, resultPair.second)
    }
}