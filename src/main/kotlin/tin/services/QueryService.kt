package tin.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import tin.model.tintheweb.FileRepository
import tin.model.tintheweb.FileType

import tin.services.technical.SystemConfigurationService
import java.io.File

@Service
@Deprecated("Deprecated as we do not allow for File Uploads yet.")
class QueryService(
    // Repositories
    private val fileRepository: FileRepository
) {
    // Services
    @Autowired
    lateinit var systemConfigurationService: SystemConfigurationService


    fun uploadQueryFile(file: MultipartFile) {
        // store the meta data
        val queryFile = tin.model.tintheweb.File(file.name, FileType.RegularPathQuery, file.size, null)
        val metaDataFile = fileRepository.save(queryFile)

        // store the file locally
        val filename = metaDataFile.id.toString()
        val storedFile = File("${systemConfigurationService.uploadPathForQueries}/$filename")
        file.transferTo(storedFile)
    }
}