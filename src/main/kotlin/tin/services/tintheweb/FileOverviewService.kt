package tin.services.tintheweb

import org.springframework.stereotype.Service
import tin.model.tintheweb.File
import tin.model.tintheweb.FileRepository
import tin.model.tintheweb.FileType

@Service
class FileOverviewService(
    // Repositories
    private val fileRepository: FileRepository
) {

    fun getAllRegularPathQueryFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.RegularPathQuery)
    }

    fun getAllDatabaseFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.Database)
    }

    fun getAllTransducerFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.Transducer)
    }

}