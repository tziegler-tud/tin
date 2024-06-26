package tin.data.tintheweb

import com.fasterxml.jackson.annotation.JsonProperty
import tin.model.tintheweb.File
import tin.model.tintheweb.FileType
import java.util.Date

class FileData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("filename") val filename: String,
    @JsonProperty("filetype") val filetype: FileType, //only custom type; simple enum
    @JsonProperty("lastModifiedAt") val lastModifiedAt: Date
) {
    constructor(model: File): this(
        id = model.id,
        filename = model.filename,
        filetype = model.filetype,
        lastModifiedAt = model.lastModifiedAt
    )
}
