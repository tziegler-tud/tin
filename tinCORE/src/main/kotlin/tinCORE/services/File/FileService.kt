package tinCORE.services.File

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

import tinCORE.services.internal.fileReaders.OntologyReaderService
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService
import tinCORE.utils.findByIdentifier

import tinCORE.data.File.*
import tinCORE.data.Task.DlTask.OntologyVariant

import tinDL.services.ontology.OntologyInfoData
import tinDL.services.ontology.OntologyManager

import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

@Service
class FileService @Autowired constructor(private val systemConfigurationService: SystemConfigurationService, private val fileRepository: TinFileRepository, private val metaRepository: OntologyMetaInfoRepository) {

    @Autowired
    lateinit var ontologyReaderService: OntologyReaderService

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

    fun addFile(uploadFile: MultipartFile, fileType: TinFileType, ontologyVariant: OntologyVariant? = null) {
        if (uploadFile.isEmpty) {
            throw Exception("Failed to add empty file.")
        }
        val filename = uploadFile.originalFilename ?: uploadFile.name;
        val fileLength = uploadFile.size

        val file = TinFile(filename, fileType, fileLength, TinFileSource.UPLOAD )
        try {
            val path = storageService.store(file, uploadFile)
            //add to repo
            file.path = path.pathString;
            fileRepository.save(file)
        }
        catch (e: Exception) {
            throw Exception("Failed to store file.")
        }
        if(fileType == TinFileType.Ontology) {
            //read ontology
            val result: FileReaderResult<File> = ontologyReaderService.read(file.path!!);
            val manager = OntologyManager(result.get());
            val info: OntologyInfoData = manager.getOntologyInfo();
            //add meta info dataset
            val meta = OntologyMetaInfo(file, info, ontologyVariant)
            metaRepository.save(meta);
        }
    }

    fun loadAsResource(file: TinFile) : Resource {
        return storageService.loadAsResource(file);
    }

    fun loadFileContent(file: TinFile) : File {
        return storageService.loadFile(file)
    }

    fun loadFileContent(fileId: Long) : File {
        try {
            var file = fileRepository.findByIdentifier(fileId);
            return storageService.loadFile(file)
        }
        catch (e: Exception) {
            throw Exception("File not found.");
        }
    }
}