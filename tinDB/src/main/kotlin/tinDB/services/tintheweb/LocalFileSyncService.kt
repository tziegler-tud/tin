package tinDB.services.tintheweb

import org.springframework.stereotype.Service
import tinDB.model.v1.tintheweb.FileRepository
import tinDB.model.v1.tintheweb.FileType
import java.io.File
import java.util.*

/**
 * use this service to sync files that have not been uploaded via the web interface
 * this applies changes to the FileRepository such that it equals the contents of the Resources/Input directories (excluding provided)
 * i.e. if the folders are empty, the Repository gets fully cleared.
 */
@Service
class LocalFileSyncService(
    private val fileRepository: FileRepository
) {

    fun syncRegularPathQueryFiles() {
        val folderPath = "src/main/resources/input/queries"
        val folder = File(folderPath)
        var preSyncRepositoryEntities = mutableListOf<tinDB.model.v1.tintheweb.File>()

        preSyncRepositoryEntities = fileRepository.findAllByFiletype(FileType.RegularPathQuery).toMutableList()

        folder.listFiles { file -> file.isFile && file.extension == "txt" }?.forEach { rpqFile ->
            val filename = rpqFile.name
            val filelength = rpqFile.length()
            val fileLastModifiedAt = Date(rpqFile.lastModified())

            // check if file exists in the repo
            val existingRepoFile = fileRepository.findByFilenameAndFiletype(filename, FileType.RegularPathQuery)

            // if it doesn't -> add it to the repo.
            if (existingRepoFile == null) {
                fileRepository.save(
                    tinDB.model.v1.tintheweb.File(
                        filename,
                        FileType.RegularPathQuery,
                        filelength,
                        fileLastModifiedAt
                    )
                )
            } else {
                // it already exists in the repo.
                // we adjust its lastModifiedAt property and then remove it from the preSyncRepositoryEntities (because we've found it)

                existingRepoFile.apply { existingRepoFile.lastModifiedAt = fileLastModifiedAt}
                preSyncRepositoryEntities.remove(existingRepoFile)
            }
        }

        // now remove any leftover entities from the db that are not present locally anymore
        fileRepository.deleteAll(preSyncRepositoryEntities)
    }

    fun syncConjunctivePathQueryFiles() {


        val folderPath = "src/main/resources/input/conjunctiveQueries"
        val folder = File(folderPath)
        var preSyncRepositoryEntities = mutableListOf<tinDB.model.v1.tintheweb.File>()

        preSyncRepositoryEntities = fileRepository.findAllByFiletype(FileType.ConjunctivePathQuery).toMutableList()

        folder.listFiles { file -> file.isFile && file.extension == "txt" }?.forEach { conjunctiveFile ->
            val filename = conjunctiveFile.name
            val filelength = conjunctiveFile.length()
            val fileLastModifiedAt = Date(conjunctiveFile.lastModified())

            // check if file exists in the repo
            val existingRepoFile = fileRepository.findByFilenameAndFiletype(filename, FileType.ConjunctivePathQuery)

            // if it doesn't -> add it to the repo.
            if (existingRepoFile == null) {
                fileRepository.save(
                    tinDB.model.v1.tintheweb.File(
                        filename,
                        FileType.ConjunctivePathQuery,
                        filelength,
                        fileLastModifiedAt
                    )
                )
            } else {
                // it already exists in the repo.
                // we adjust its lastModifiedAt property and then remove it from the preSyncRepositoryEntities (because we've found it)

                existingRepoFile.apply { existingRepoFile.lastModifiedAt = fileLastModifiedAt}
                preSyncRepositoryEntities.remove(existingRepoFile)
            }
        }

        // now remove any leftover entities from the db that are not present locally anymore
        fileRepository.deleteAll(preSyncRepositoryEntities)


    }

    fun syncDatabaseFiles() {
        val folderPath = "src/main/resources/input/databases"
        val folder = File(folderPath)
        var preSyncRepositoryEntities = mutableListOf<tinDB.model.v1.tintheweb.File>()

        preSyncRepositoryEntities = fileRepository.findAllByFiletype(FileType.Database).toMutableList()

        folder.listFiles { file -> file.isFile && file.extension == "txt" }?.forEach { databaseFile ->
            val filename = databaseFile.name
            val filelength = databaseFile.length()
            val fileLastModifiedAt = Date(databaseFile.lastModified())


            // check if file exists in the repo
            val existingRepoFile = fileRepository.findByFilenameAndFiletype(filename, FileType.Database)

            // if it doesn't -> add it to the repo.
            if (existingRepoFile == null) {
                fileRepository.save(
                    tinDB.model.v1.tintheweb.File(
                        filename,
                        FileType.Database,
                        filelength,
                        fileLastModifiedAt
                    )
                )
            } else {
                // it already exists in the repo.
                // we adjust its lastModifiedAt property and then remove it from the preSyncRepositoryEntities (because we've found it)

                existingRepoFile.apply { existingRepoFile.lastModifiedAt = fileLastModifiedAt}
                preSyncRepositoryEntities.remove(existingRepoFile)
            }
        }

        // now remove any leftover entities from the db that are not present locally anymore
        fileRepository.deleteAll(preSyncRepositoryEntities)
    }

    fun syncTransducerFiles() {
        val folderPath = "src/main/resources/input/transducers"
        val folder = File(folderPath)
        var preSyncRepositoryEntities = mutableListOf<tinDB.model.v1.tintheweb.File>()

        preSyncRepositoryEntities = fileRepository.findAllByFiletype(FileType.Transducer).toMutableList()

        folder.listFiles { file -> file.isFile && file.extension == "txt" }?.forEach { transducerFile ->
            val filename = transducerFile.name
            val filelength = transducerFile.length()
            val fileLastModifiedAt = Date(transducerFile.lastModified())


            // check if file exists in the repo
            val existingRepoFile = fileRepository.findByFilenameAndFiletype(filename, FileType.Transducer)

            // if it doesn't -> add it to the repo.
            if (existingRepoFile == null) {
                fileRepository.save(
                    tinDB.model.v1.tintheweb.File(
                        filename,
                        FileType.Transducer,
                        filelength,
                        fileLastModifiedAt
                    )
                )
            } else {
                // it already exists in the repo.
                // we adjust its lastModifiedAt property and then remove it from the preSyncRepositoryEntities (because we've found it)

                existingRepoFile.apply { existingRepoFile.lastModifiedAt = fileLastModifiedAt}
                preSyncRepositoryEntities.remove(existingRepoFile)
            }
        }

        // now remove any leftover entities from the db that are not present locally anymore
        fileRepository.deleteAll(preSyncRepositoryEntities)
    }


}