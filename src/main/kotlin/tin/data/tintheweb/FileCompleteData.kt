package tin.data.tintheweb

import tin.model.File
import java.util.*

class FileCompleteData(
    val id: Long,
    val filename: String,
    val uploadDate: Date,
) {
    constructor(model: File):this(
        id = model.id,
        filename = model.filename,
        uploadDate = model.createdAt,
    )
}

