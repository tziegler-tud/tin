package tinLIB.services.internal.fileReaders.fileReaderResult

import tinLIB.services.internal.fileReaders.FileReaderWarning

abstract class AbstractFileReaderResult(
    var warnings: MutableList<FileReaderWarning>,
    var errors: MutableList<FileReaderWarning>,
)