package tinCORE.services.internal.fileReaders.fileReaderResult

import tinCORE.services.internal.fileReaders.FileReaderWarning

class FileReaderResult<T>(
    var graph: T,
    warnings: MutableList<FileReaderWarning>,
    errors: MutableList<FileReaderWarning>,
) : AbstractFileReaderResult(warnings, errors) {

    fun get(): T {
        return this.graph;
    }
}