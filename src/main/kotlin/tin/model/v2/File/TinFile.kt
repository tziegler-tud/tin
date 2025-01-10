package tin.model.v2.File

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class TinFile(
    val filename: String,
    val filetype: TinFileType,
    val filelength: Long,
    val source: TinFileSource,
    lastModifiedAt: Date?) {
    @GeneratedValue
    @Id
    val id: Long = 0

    var lastModifiedAt: Date = lastModifiedAt?: Date()

}


interface TinFileRepository : JpaRepository<TinFile, Long>{
    fun findAllByFiletype(fileType: TinFileType): List<TinFile>

    fun findByFilenameAndFiletype(filename: String, filetype: TinFileType): TinFile?

    fun findByIdAndFiletype(id: Long, filetype: TinFileType): TinFile?
}