package tin.services.Task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tin.model.v2.Tasks.TaskComputationConfiguration
import tin.model.v2.Tasks.TaskFileConfiguration
import tin.model.v2.Tasks.TaskRuntimeConfiguration
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
    service: FileService
) {
    @Autowired
    final lateinit var systemConfigurationService: SystemConfigurationService;

    private val taskQueue: TaskQueue = TaskQueue();
    private var isProcessing: Boolean = false;

    private val queryFileReader = QueryReaderServiceV2(systemConfigurationService);
    private val transducerFileReader = TransducerReaderServiceV2(systemConfigurationService);
    private val ontologyReader = OntologyReaderService(systemConfigurationService);


    fun createTask(taskFileConfiguration: TaskFileConfiguration, taskRuntimeConfiguration: TaskRuntimeConfiguration, taskComputationConfiguration: TaskComputationConfiguration): Task {
        return Task(taskFileConfiguration, taskRuntimeConfiguration, taskComputationConfiguration);
    }

    /**
     * adds a task to the queue
     * @return boolean returns true if the queue was empty before the insertion
     */
    fun addTask(task: Task) : Boolean{
        val isEmpty = taskQueue.isEmpty();
        taskQueue.add(task);
        return isEmpty;
    }

    fun getTasks() : TaskQueue{
        return taskQueue;
    }

    fun processNext() : Boolean {
        if(taskQueue.isEmpty()) return false;
        if(isProcessing) return false;
        val task = taskQueue.getNext()
        if(task == null) return false;
        processTask(task);
        return true;
    }

    private fun processTask(task: Task) {
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
        task.setState(TaskStatus.Calculating)

        val resultPair: Pair<List<ShortestPathResult>, TaskProcessingBenchmarkResult> = processor.execute();

        //save results to DB?

    }

    private fun readTransducerFile(){

    }


}