package tin.data.tintheweb

import tin.model.tintheweb.File

class FileCompleteData(
    val id: Long,
    val filename: String,
) {
    constructor(model: File):this(
        id = model.id,
        filename = model.filename,
    )
}

