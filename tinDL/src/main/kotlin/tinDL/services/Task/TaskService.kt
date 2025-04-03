package tinDL.services.Task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tinDL.model.v2.Tasks.*
import tinDL.model.v2.transducer.TransducerGraph
import tinDL.services.Task.Benchmark.TaskProcessingBenchmarkResult
import tinDL.services.files.FileService
import tinDL.services.internal.fileReaders.OntologyReaderService
import tinDL.services.internal.fileReaders.QueryReaderServiceV2
import tinDL.services.internal.fileReaders.TransducerReaderServiceV2
import tinDL.services.ontology.OntologyManager
import tinDL.services.ontology.ResultGraph.ShortestPathResult
import tinDL.services.technical.SystemConfigurationService

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

        val queryFile = fileService.getFile(fileConfiguration.queryFileIdentifier)!!;
        val ontologyFile = fileService.getFile(fileConfiguration.ontologyFileIdentifier)!!;

        val qf = fileService.loadFileContent(queryFile);
        val of = fileService.loadFileContent(ontologyFile);

        //file readers
        //read and check for error
        val queryResult = queryFileReader.processFile(qf, false)
        val ontologyResult = ontologyReader.processFile(of, false)

        val manager: OntologyManager = OntologyManager(ontologyResult.get())
        val processor: TaskProcessor;
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

        processor = TaskProcessor(task, manager, queryResult.get(), fileConfiguration.transducerMode, fileConfiguration.transducerGenerationMode, transducerGraph);

        val resultPair: Pair<List<ShortestPathResult>, TaskProcessingBenchmarkResult> = processor.execute();
        isProcessing = false;
        //save results to DB?
        return ProcessingResult(ProcessingResultStatus.SUCCESS, resultPair.first, resultPair.second)
    }
}