package tinDL.services.internal.fileReaders.fileReaderResult

import tinDL.services.internal.fileReaders.FileReaderWarning

abstract class AbstractFileReaderResult(
    var warnings: MutableList<FileReaderWarning>,
    var errors: MutableList<FileReaderWarning>,
)