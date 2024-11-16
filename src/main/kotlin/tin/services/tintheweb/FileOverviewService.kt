package tin.services.tintheweb

import org.springframework.stereotype.Service
import tin.model.v1.tintheweb.File
import tin.model.v1.tintheweb.FileRepository
import tin.model.v1.tintheweb.FileType

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

    fun getAllOntologyFiles(): List<File> {
        return fileRepository.findAllByFiletype(FileType.Ontology)
    }

}