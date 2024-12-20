package tin.model.v2.File

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class TinFile(
    val filename: String,
    val filetype: FileType,
    val filelength: Long,
    lastModifiedAt: Date?) {
    @GeneratedValue
    @Id
    val id: Long = 0

    var lastModifiedAt: Date = lastModifiedAt?: Date()

}


interface FileRepository : JpaRepository<TinFile, Long>{
    fun findAllByFiletype(fileType: FileType): List<TinFile>

    fun findByFilenameAndFiletype(filename: String, filetype: FileType): TinFile?

    fun findByIdAndFiletype(id: Long, filetype: FileType): TinFile?
}