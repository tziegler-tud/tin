package tinDB.services.internal.fileReaders.fileReaderResult

import tinDB.services.internal.fileReaders.FileReaderWarning

abstract class AbstractFileReaderResult(
    var warnings: MutableList<FileReaderWarning>,
    var errors: MutableList<FileReaderWarning>,
)