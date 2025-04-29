package tinDB.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import tinDB.model.v1.tintheweb.FileRepository
import tinDB.model.v1.tintheweb.FileType
import tinDB.services.technical.SystemConfigurationService
import java.io.File

@Service
@Deprecated("Deprecated as we do not allow for File Uploads yet.")
class DatabaseService(
    // Repositories
    private val fileRepository: FileRepository
) {
    // Services
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService

    fun uploadDatabaseFile(file: MultipartFile) {
        // store the meta data
        val databaseFile = tinDB.model.v1.tintheweb.File(file.name, FileType.Database, file.size, null)
        val metaDataFile = fileRepository.save(databaseFile)

        // store the file locally
        val filename = metaDataFile.id.toString()
        val storedFile = File("${systemConfigurationService.getDatabasePath()}/$filename")
        file.transferTo(storedFile)
    }

    fun getAllDatabaseMetaData(): List<tinDB.model.v1.tintheweb.File> {
        println("getAllDatabaseMetaData")
        return fileRepository.findAllByFiletype(FileType.Database)
    }
}