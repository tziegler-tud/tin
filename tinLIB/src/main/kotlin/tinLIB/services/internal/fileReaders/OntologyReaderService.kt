package tinLIB.services.internal.fileReaders

import tinLIB.services.internal.fileReaders.fileReaderResult.FileReaderResult
import java.io.File

class OntologyReaderService(
    filePath: String,
) : FileReaderService<FileReaderResult<File>>(
    filePath
) {

    override fun processFile(file: File, breakOnError: Boolean): FileReaderResult<File> {
        return FileReaderResult<File>(file, this.warnings, this.errors);
    }
}