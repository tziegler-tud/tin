package tin.services.internal.fileReaders

class FileReaderResult<T>(
        var graph: T,
        var warnings: MutableList<FileReaderWarning>,
        var errors: MutableList<FileReaderWarning>,
){
    fun get(): T{
        return this.graph;
    }
}