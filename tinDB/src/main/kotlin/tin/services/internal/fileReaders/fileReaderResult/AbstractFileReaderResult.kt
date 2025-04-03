package tin.services.internal.fileReaders.fileReaderResult

import tin.services.internal.fileReaders.FileReaderWarning

abstract class AbstractFileReaderResult(
    var warnings: MutableList<FileReaderWarning>,
    var errors: MutableList<FileReaderWarning>,
)