package tinDL.data.tintheweb

import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.model.v2.File.TinFile
import tinDL.model.v2.File.TinFileType
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
