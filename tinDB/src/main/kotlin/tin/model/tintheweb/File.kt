package tin.model.tintheweb

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

@Entity
class File(
    val filename: String,
    val filetype: FileType,
    val filelength: Long,
    lastModifiedAt: Date?) {
    @GeneratedValue
    @Id
    val id: Long = 0

    var lastModifiedAt: Date = lastModifiedAt?: Date()

}


interface FileRepository : JpaRepository<File, Long>{
    fun findAllByFiletype(fileType: FileType): List<File>

    fun findByFilenameAndFiletype(filename: String, filetype: FileType): File?

    fun findByIdAndFiletype(id: Long, filetype: FileType): File?
}