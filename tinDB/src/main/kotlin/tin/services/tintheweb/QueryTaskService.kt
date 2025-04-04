package tin.services.tintheweb

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestBody
import tin.data.tintheweb.queryTask.QueryTaskCreateData
import tin.model.queryResult.QueryResultStatus
import tin.model.queryTask.ComputationProperties
import tin.model.queryTask.ComputationPropertiesRepository
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
    private val computationPropertiesRepository: ComputationPropertiesRepository,
) {

    fun getAllQueryTasks(): List<QueryTask> {
        return queryTaskRepository.findAll()
    }

    @Transactional
    fun createQueryTask(@RequestBody data: QueryTaskCreateData): QueryTask {

        // check for potential errors before we queue the task.
        // first: check, if the files are present.
        var errorWhileReadingFiles: QueryResultStatus = QueryResultStatus.NoError
        val tempQueryFile: File? =
            fileRepository.findByIdAndFiletype(data.queryFileIdentifier, FileType.RegularPathQuery)
        val tempDatabaseFile: File? = fileRepository.findByIdAndFiletype(data.databaseFileIdentifier, FileType.Database)

        if (data.transducerFileIdentifier != null && !data.computationProperties.generateTransducer) {
            // transducerFile is present AND it should be used as well
            val tempTransducerFile: File? =
                fileRepository.findByIdAndFiletype(data.transducerFileIdentifier, FileType.Transducer)

            if (tempTransducerFile == null) errorWhileReadingFiles =
                QueryResultStatus.TransducerFileNotFound
        }

        if (tempQueryFile == null) errorWhileReadingFiles = QueryResultStatus.QueryFileNotFound
        if (tempDatabaseFile == null) errorWhileReadingFiles = QueryResultStatus.DatabaseFileNotFound

        // compProperties.id should really not fail but if it somehow does, we still have the fallback option.
        // Note: If you change the dataClass, you can simply throw away the catch block.
        // it should not be needed in the first place, since the compProperties object should be created by the user.
        val computationProperties: ComputationProperties = try {
            computationPropertiesRepository.findById(data.computationProperties.id).get()
        } catch (e: Exception) {
            // e: NoSuchElementException but we don't use it anyway
            ComputationProperties(
                topKValue = data.computationProperties.topKValue,
                thresholdValue = data.computationProperties.thresholdValue,
                generateTransducer = data.computationProperties.generateTransducer,
                transducerGeneration = data.computationProperties.transducerGeneration,
                name = data.computationProperties.name,
                computationModeEnum = data.computationProperties.computationModeEnum,
            )
        }

        val queryTask = QueryTask(
            data.queryFileIdentifier,
            data.transducerFileIdentifier,
            data.databaseFileIdentifier,
            QueryTask.QueryStatus.Error,
            QueryTask.QueryType.regularPathQuery,
            null,
            computationProperties
        )

        // if an error is found, we don't queue the task
        if (errorWhileReadingFiles in listOf(
                QueryResultStatus.QueryFileNotFound,
                QueryResultStatus.TransducerFileNotFound,
                QueryResultStatus.DatabaseFileNotFound
            )
        ) {
            queryTask.queryStatus = QueryTask.QueryStatus.Error
        } else {

            // no major error found.
            queryTask.queryStatus = QueryTask.QueryStatus.Queued
        }

        return queryTaskRepository.save(queryTask)
    }
}

