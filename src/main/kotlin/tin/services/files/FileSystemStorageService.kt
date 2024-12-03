package tin.services.files

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import tin.services.technical.SystemConfigurationService
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Stream


@Service
class FileSystemStorageService(systemConfigurationService: SystemConfigurationService) {
    private val uploadPath = systemConfigurationService.getUploadParentPath()
    private val uploadLocation = Path.of(uploadPath)
    private val queryLocation = Path.of(systemConfigurationService.getUploadQueryPath())
    private val transducerLocation = Path.of(systemConfigurationService.getUploadTransducerPath())
    private val ontologyLocation = Path.of(systemConfigurationService.getUploadOntologyPath())

    init {
        init();
    }

    fun storeQueryFile(file: MultipartFile?) {
        return store(queryLocation, file);
    }

    fun storeTransducerFile(file: MultipartFile?) {
        return store(transducerLocation, file);
    }

    fun storeOntologyFile(file: MultipartFile?) {
        return store(ontologyLocation, file);
    }

    fun store(path: Path, file: MultipartFile?) {
        try {
            if (file!!.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }

            val destinationFile = path.resolve(
                Paths.get(file.originalFilename)
            )
                .normalize().toAbsolutePath()
            if (destinationFile.parent != path.toAbsolutePath()) {
                // This is a security check
                throw StorageException(
                    "Cannot store file outside current directory."
                )
            }
            file.inputStream.use { inputStream ->
                Files.copy(
                    inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (e: IOException) {
            throw StorageException("Failed to store file.", e)
        }
    }

    fun loadAll(): Stream<Path?>? {
        try {
            return Files.walk(this.uploadLocation, 1)
                .filter { path: Path -> path != this.uploadLocation }
                .map { other: Path? -> uploadLocation.relativize(other) }
        } catch (e: IOException) {
            throw StorageException("Failed to read stored files", e)
        }
    }

    fun loadQueryFile(filename: String?): Resource {
        try {
            return loadAsResource(queryLocation, filename)
        }
        catch (e: StorageFileNotFoundException) {
            throw StorageFileNotFoundException("Could not read query file: $filename", e)
        }
    }

    fun loadTransducerFile(filename: String?): Resource {
        try {
            return loadAsResource(transducerLocation, filename)
        }
        catch (e: StorageFileNotFoundException) {
            throw StorageFileNotFoundException("Could not read transducer file: $filename", e)
        }
    }

    fun loadOntologyFile(filename: String?): Resource {
        try {
            return loadAsResource(ontologyLocation, filename)
        }
        catch (e: StorageFileNotFoundException) {
            throw StorageFileNotFoundException("Could not read ontology file: $filename", e)
        }
    }

    private fun load(path: Path, filename: String?): Path? {
        if(filename == null) return null;
        return path.resolve(filename)
    }

    private fun loadAsResource(absPath: Path, filename: String?): Resource {
        try {
            val file = load(absPath, filename)
            val resource: Resource = UrlResource(file!!.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            } else {
                throw StorageFileNotFoundException(
                    "Could not read file: $filename"
                )
            }
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: ${absPath.toString()} , $filename", e)
        }
    }

    fun deleteAll() {
        FileSystemUtils.deleteRecursively(uploadLocation.toFile())
    }

    private final fun init() {
        try {
            Files.createDirectories(uploadLocation)
            Files.createDirectories(queryLocation)
            Files.createDirectories(transducerLocation)
            Files.createDirectories(ontologyLocation)
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }
}