package tinLIB.services.files

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream


interface StorageService {
    fun init()

    fun storeQueryFile(file: MultipartFile?)

    fun storeTransducerFile(file: MultipartFile?)

    fun storeOntologyFile(file: MultipartFile?)

    fun loadAll(): Stream<Path?>?

    fun load(path: Path, filename: String?): Path?

    fun loadAsResource(path: Path, filename: String?): Resource?

    fun deleteAll()
}