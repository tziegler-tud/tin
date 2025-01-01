package tin.services.files

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import tin.model.v2.File.TinFile
import tin.model.v2.File.TinFileType
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Stream

class FileSystemStorageService(
            private val uploadLocation: Path,
            private val queryLocation: Path,
            private val transducerLocation: Path,
            private val ontologyLocation: Path,
)
{

    private fun getPath(fileType: TinFileType): Path {
        return when(fileType) {
            TinFileType.RegularPathQuery -> queryLocation
            TinFileType.Transducer -> transducerLocation
            TinFileType.Ontology -> ontologyLocation
        }
    }

    fun store(file: TinFile, content: MultipartFile) {
        try {
            if (content.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }

            val path = getPath(file.filetype)

            val destinationFile = path.resolve(
                Paths.get(file.filename)
            )
                .normalize().toAbsolutePath()
            if (destinationFile.parent != path.toAbsolutePath()) {
                // This is a security check
                throw StorageException(
                    "Cannot store file outside current directory."
                )
            }
            content.inputStream.use { inputStream ->
                Files.copy(
                    inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (e: IOException) {
            throw StorageException("Failed to store file.", e)
        }
    }

    fun loadAsResource(file: TinFile) : Resource {
        return loadAsResource(getPath(file.filetype), file.filename)
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

    fun loadFile(file: TinFile) : File {
        return loadAsFile(getPath(file.filetype), file.filename);
    }

    private fun loadPath(path: Path, filename: String?): Path? {
        if(filename == null) return null;
        return path.resolve(filename)
    }

    private fun loadAsResource(absPath: Path, filename: String?): Resource {
        try {
            val file = loadPath(absPath, filename)
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

    private fun loadAsFile(absPath: Path, filename: String?): File {
        try {
            val file = loadPath(absPath, filename)
            return file!!.toFile();
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: ${absPath.toString()} , $filename", e)
        }
    }


    fun deleteAll() {
        FileSystemUtils.deleteRecursively(uploadLocation.toFile())
    }

    fun init() {
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