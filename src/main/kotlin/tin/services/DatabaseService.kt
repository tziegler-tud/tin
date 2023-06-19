package tin.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import tin.model.FileRepository
import tin.model.FileType
import tin.services.technical.SystemConfigurationService
import java.io.File

@Service
class DatabaseService(
    // Repositories
    private val fileRepository: FileRepository
) {
    // Services
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    fun uploadDatabaseFile(file: MultipartFile) {
        // store the meta data
        val databaseFile = tin.model.File(file.name, FileType.Database)
        val metaDataFile = fileRepository.save(databaseFile)

        // store the file locally
        val filename = metaDataFile.id.toString()
        val storedFile = File("${systemConfigurationService.uploadPathForDatabases}/$filename")
        file.transferTo(storedFile)
    }

    fun getAllDatabaseMetaData(): List<tin.model.File> {
        println("getAllDatabaseMetaData")
        return fileRepository.findAllByFiletype(FileType.Database)
    }
}