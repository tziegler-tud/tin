package tin.model.v1.tintheweb

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

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