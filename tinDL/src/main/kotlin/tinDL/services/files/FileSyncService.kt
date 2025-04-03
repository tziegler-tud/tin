package tinDL.services.files

import org.springframework.stereotype.Service
import tinDL.model.v2.File.TinFile
import tinDL.model.v2.File.TinFileRepository
import tinDL.model.v2.File.TinFileSource
import tinDL.model.v2.File.TinFileType
import tinDL.services.technical.SystemConfigurationService
import java.io.File
import java.util.*

/**
 * use this service to sync files that have not been uploaded via the web interface
 * this applies changes to the FileRepository such that it equals the contents of the Resources/Input directories (excluding provided)
 * i.e. if the folders are empty, the Repository gets fully cleared.
 */
@Service
class FileSyncService(
    private val fileRepository: TinFileRepository,
    private val systemConfigurationService: SystemConfigurationService,
) {
    fun syncAll(){
        syncRegularPathQueryFilesUpload();
        syncRegularPathQueryFilesProvided()
        syncTransducerFilesUpload();
        syncTransducerFilesProvided()
        syncOntologyFilesUpload()
        syncOntologyFilesProvided()
    }

    fun syncRegularPathQueryFilesProvided() {
        val folderPath = systemConfigurationService.getQueryPath();
        syncFiles(folderPath, TinFileType.RegularPathQuery, TinFileSource.PROVIDED)
    }

    fun syncRegularPathQueryFilesUpload() {
        val folderPath = systemConfigurationService.getUploadQueryPath();
        syncFiles(folderPath, TinFileType.RegularPathQuery, TinFileSource.UPLOAD)
    }

    fun syncTransducerFilesProvided() {
        val folderPath = systemConfigurationService.getTransducerPath()
        syncFiles(folderPath, TinFileType.Transducer, TinFileSource.PROVIDED)
    }

    fun syncTransducerFilesUpload() {
        val folderPath = systemConfigurationService.getUploadTransducerPath()
        syncFiles(folderPath, TinFileType.Transducer, TinFileSource.UPLOAD)
    }

    fun syncOntologyFilesProvided() {
        val folderPath = systemConfigurationService.getOntologyPath()
        syncFiles(folderPath, TinFileType.Ontology, TinFileSource.PROVIDED)
    }


    fun syncOntologyFilesUpload() {
        val folderPath = systemConfigurationService.getUploadOntologyPath()
        syncFiles(folderPath, TinFileType.Ontology, TinFileSource.UPLOAD)
    }

    private fun syncFiles(path: String, type: TinFileType, source: TinFileSource) {
        val folder = File(path)
        var preSyncRepositoryEntities = mutableListOf<TinFile>()

        preSyncRepositoryEntities = fileRepository.findAllByFiletype(type).toMutableList();

        val allowedExtensions = when(type){
            TinFileType.RegularPathQuery -> listOf("txt", "tinput")
            TinFileType.Transducer -> listOf("txt", "tinput")
            TinFileType.Ontology  -> listOf("rdf", "owl")
        }

        folder.listFiles { f -> f.isFile && allowedExtensions.contains(f.extension)}?.forEach { file ->
            val filename = file.name
            val filelength = file.length()
            val fileLastModifiedAt = Date(file.lastModified())


            // check if file exists in the repo
            val existingRepoFile = fileRepository.findByFilenameAndFiletype(filename, type)

            // if it doesn't -> add it to the repo.
            if (existingRepoFile == null) {
                fileRepository.save(
                    TinFile(
                        filename,
                        type,
                        filelength,
                        source,
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
        //fileRepository.deleteAll(preSyncRepositoryEntities)
    }


}