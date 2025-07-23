package tinDB.services.tintheweb

import org.springframework.stereotype.Service
import tinDB.model.v1.tintheweb.File
import tinDB.model.v1.tintheweb.FileRepository
import tinDB.model.v1.tintheweb.FileType

@Service
class FileOverviewService(
    // Repositories
    private val fileRepository: FileRepository
) {

    fun getAllRegularPathQueryFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.RegularPathQuery)
    }

    fun getAllConjunctivePathQueryFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.ConjunctivePathQuery)
    }

    fun getAllDatabaseFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.Database)
    }

    fun getAllTransducerFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.Transducer)
    }

}