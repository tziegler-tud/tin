package tin.services.files

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import tin.model.v2.File.FileRepository
import tin.model.v2.File.FileType
import tin.model.v2.File.TinFile
import tin.services.technical.SystemConfigurationService
import tin.utils.findByIdentifier
import java.io.File
import java.nio.file.Path
import java.util.*

@Service
class FileService @Autowired constructor(private val systemConfigurationService: SystemConfigurationService, private val fileRepository: FileRepository) {

    private val uploadPath = systemConfigurationService.getUploadParentPath()
    private val uploadLocation = Path.of(uploadPath)
    private val queryPath = Path.of(systemConfigurationService.getUploadQueryPath());
    private val transducerPath = Path.of(systemConfigurationService.getUploadTransducerPath());
    private val ontologyPath = Path.of(systemConfigurationService.getUploadOntologyPath());

    private val storageService: FileSystemStorageService = FileSystemStorageService(
        uploadLocation, queryPath, transducerPath, ontologyPath
    )

    init {
        try {
            storageService.init();
        }
        catch (e: Exception) {
            throw Exception("Failed to initialize storageService.", e)
        }
    }

    fun getFile(queryFileIdetifier: Long) : TinFile? {
        val file = fileRepository.findByIdentifier(queryFileIdetifier);
        return file;
    }

    private fun addFile(file: TinFile) {

        fileRepository.save(file);
    }

    fun addOntology(uploadFile: MultipartFile) {
        addFile(uploadFile, FileType.Ontology)
    }

    fun addQuery(uploadFile: MultipartFile) {
        addFile(uploadFile, FileType.RegularPathQuery)
    }

    fun addTransducer(uploadFile: MultipartFile) {
        addFile(uploadFile, FileType.Transducer)
    }

    fun addFile(uploadFile: MultipartFile, fileType: FileType) {
        if (uploadFile.isEmpty) {
            throw Exception("Failed to add empty file.")
        }
        val filename = uploadFile.originalFilename ?: uploadFile.name;
        val path = ontologyPath;
        val fileLength = uploadFile.size

        val file = TinFile(filename, fileType, fileLength, Date())
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