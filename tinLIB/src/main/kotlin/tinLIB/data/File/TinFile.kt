package tinLIB.data.File

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

@Entity
class TinFile(
    val filename: String = "",
    val filetype: TinFileType = TinFileType.File,
    val filelength: Long = 0,
    val source: TinFileSource = TinFileSource.UNKNOWN,
    var path: String? = "",
    lastModifiedAt: Date? = null) {

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