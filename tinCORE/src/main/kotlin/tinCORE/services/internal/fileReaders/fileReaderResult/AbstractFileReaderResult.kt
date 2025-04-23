package tinCORE.services.internal.fileReaders.fileReaderResult

import tinCORE.services.internal.fileReaders.FileReaderWarning


abstract class AbstractFileReaderResult(
    var warnings: MutableList<FileReaderWarning>,
    var errors: MutableList<FileReaderWarning>,
)