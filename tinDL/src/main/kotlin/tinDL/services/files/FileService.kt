package tinDL.services.files

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import tinDL.model.v2.File.TinFileRepository
import tinDL.model.v2.File.TinFileType
import tinDL.model.v2.File.TinFile
import tinDL.model.v2.File.TinFileSource
import tinDL.services.technical.SystemConfigurationService
import tinDL.utils.findByIdentifier
import java.io.File
import java.nio.file.Path
import java.util.*

@Service
class FileService @Autowired constructor(private val systemConfigurationService: SystemConfigurationService, private val fileRepository: TinFileRepository) {

    private val uploadPath = systemConfigurationService.getUploadParentPath()
    private val uploadLocation = Path.of(uploadPath)
    private val providedQueryPath = Path.of(systemConfigurationService.getQueryPath())
    private val providedTransducerPath = Path.of(systemConfigurationService.getTransducerPath())
    private val providedOntologyPath = Path.of(systemConfigurationService.getOntologyPath())
    private val uploadQueryPath = Path.of(systemConfigurationService.getUploadQueryPath());
    private val uploadTransducerPath = Path.of(systemConfigurationService.getUploadTransducerPath());
    private val uploadOntologyPath = Path.of(systemConfigurationService.getUploadOntologyPath());

    private val storageService: FileSystemStorageService = FileSystemStorageService(
        uploadLocation, uploadQueryPath, uploadTransducerPath, uploadOntologyPath, providedQueryPath, providedTransducerPath, providedOntologyPath
    )

    private val localSyncService = FileSyncService(fileRepository, systemConfigurationService)

    init {
        try {
            storageService.init();
        }
        catch (e: Exception) {
            throw Exception("Failed to initialize storageService.", e)
        }
    }

    fun syncLocalFiles(){
        localSyncService.syncAll();
    }

    fun getAllQueryFiles(): List<TinFile> {
        return fileRepository.findAllByFiletype(TinFileType.RegularPathQuery)
    }

    fun getAllTransducerFiles(): List<TinFile> {
        return fileRepository.findAllByFiletype(TinFileType.Transducer)
    }

    fun getAllOntologyFiles(): List<TinFile> {
        return fileRepository.findAllByFiletype(TinFileType.Ontology)
    }

    fun getFile(queryFileIdetifier: Long) : TinFile? {
        val file = fileRepository.findByIdentifier(queryFileIdetifier);
        return file;
    }

    private fun addFile(file: TinFile) {

        fileRepository.save(file);
    }

    fun addOntology(uploadFile: MultipartFile) {
        addFile(uploadFile, TinFileType.Ontology)
    }

    fun addQuery(uploadFile: MultipartFile) {
        addFile(uploadFile, TinFileType.RegularPathQuery)
    }

    fun addTransducer(uploadFile: MultipartFile) {
        addFile(uploadFile, TinFileType.Transducer)
    }

    fun addFile(uploadFile: MultipartFile, fileType: TinFileType) {
        if (uploadFile.isEmpty) {
            throw Exception("Failed to add empty file.")
        }
        val filename = uploadFile.originalFilename ?: uploadFile.name;
        val fileLength = uploadFile.size

        val file = TinFile(filename, fileType, fileLength, TinFileSource.UPLOAD, Date())
        try {
            storageService.store(file, uploadFile)
            //add to repo
            fileRepository.save(file)
        }
        catch (e: Exception) {
            throw Exception("Failed to store file.")

        }
    }

    fun loadAsResource(file: TinFile) : Resource {
        return storageService.loadAsResource(file);
    }

    fun loadFileContent(file: TinFile) : File {
        return storageService.loadFile(file)
    }

    fun loadFileContent(fileId: Long) : File {
        val file = fileRepository.findByIdentifier(fileId);
        if(file == null) throw Exception("File not found.");
        return storageService.loadFile(file)
    }
}