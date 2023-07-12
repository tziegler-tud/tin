package tin.services.tintheweb

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestBody
import tin.data.tintheweb.queryTask.ComputationModeData
import tin.data.tintheweb.queryTask.QueryTaskData
import tin.model.queryResult.QueryResult
import tin.model.queryTask.ComputationMode
import tin.model.queryTask.ComputationProperties
import tin.model.queryTask.QueryTask
import tin.model.queryTask.QueryTaskRepository
import tin.model.tintheweb.File
import tin.model.tintheweb.FileRepository
import tin.model.tintheweb.FileType

@Service
class QueryTaskService(
    // Repositories
    private val queryTaskRepository: QueryTaskRepository,
    private val fileRepository: FileRepository,
) {

    fun getAllQueryTasks(): List<QueryTask> {
        return queryTaskRepository.findAll()
    }

    @Transactional
    fun createQueryTask(@RequestBody data: QueryTaskData): QueryTask {
        val queryTask: QueryTask

        // check for potential errors before we queue the task.
        // first: check, if the files are present.
        var errorWhileReadingFiles: QueryResult.QueryResultStatus = QueryResult.QueryResultStatus.NoError
        val tempQueryFile: File? =
            fileRepository.findByIdAndFiletype(data.queryFileIdentifier, FileType.RegularPathQuery)
        val tempDatabaseFile: File? = fileRepository.findByIdAndFiletype(data.databaseFileIdentifier, FileType.Database)

        if (data.transducerFileIdentifier != null && !data.computationMode.computationProperties.generateTransducer) {
            // transducerFile is present AND it should be used as well
            val tempTransducerFile: File? =
                fileRepository.findByIdAndFiletype(data.transducerFileIdentifier, FileType.Transducer)

            if (tempTransducerFile == null) errorWhileReadingFiles =
                QueryResult.QueryResultStatus.TransducerFileNotFound
        }

        if (tempQueryFile == null) errorWhileReadingFiles = QueryResult.QueryResultStatus.QueryFileNotFound
        if (tempDatabaseFile == null) errorWhileReadingFiles = QueryResult.QueryResultStatus.DatabaseFileNotFound

        // if an error is found, we don't queue the task
        if (errorWhileReadingFiles in listOf(
                QueryResult.QueryResultStatus.QueryFileNotFound,
                QueryResult.QueryResultStatus.TransducerFileNotFound,
                QueryResult.QueryResultStatus.DatabaseFileNotFound
            )
        ) {
            queryTask = QueryTask(
                data.queryFileIdentifier,
                data.transducerFileIdentifier,
                data.databaseFileIdentifier,
                QueryTask.QueryStatus.Error,
                null,
                transformComputationPropertiesDataToModel(data.computationMode)
            )
        } else {
            // no major error found.

            queryTask = QueryTask(
                data.queryFileIdentifier,
                data.transducerFileIdentifier,
                data.databaseFileIdentifier,
                QueryTask.QueryStatus.Queued,
                null,
                transformComputationPropertiesDataToModel(data.computationMode)
            )
        }
        return queryTaskRepository.save(queryTask)
    }

    private fun transformComputationPropertiesDataToModel(data: ComputationModeData): ComputationMode {
        return ComputationMode(
            data.computationModeEnum,
            ComputationProperties(
                data.computationProperties.topKValue,
                data.computationProperties.thresholdValue,
                data.computationProperties.generateTransducer,
                data.computationProperties.transducerGeneration
            )
        )
    }
}

