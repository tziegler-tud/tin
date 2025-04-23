package tinCORE.data.api

import com.fasterxml.jackson.annotation.JsonProperty
import tinCORE.data.File.TinFile
import tinCORE.data.File.TinFileType
import java.util.Date

class TinFileData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("filename") val filename: String,
    @JsonProperty("filetype") val filetype: TinFileType, //only custom type; simple enum
    @JsonProperty("lastModifiedAt") val lastModifiedAt: Date
) {
    constructor(model: TinFile): this(
        id = model.id,
        filename = model.filename,
        filetype = model.filetype,
        lastModifiedAt = model.lastModifiedAt
    )
}
