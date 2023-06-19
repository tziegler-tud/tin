package tin.model

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class File(
    val filename: String,
    val filetype: FileType) {
    @GeneratedValue
    @Id
    val id: Long = 0

    val createdAt: Date = Date()

    var lastModifiedAt: Date = Date()
}

enum class FileType {
    RegularPathQuery,
    Database,
    Transducer,
}

interface FileRepository : JpaRepository<File, Long>{
    fun findAllByFiletype(fileType: FileType): List<File>
}