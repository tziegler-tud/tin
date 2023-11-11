package tin.services.internal.fileReaders

import org.springframework.stereotype.Service
import tin.model.graph.Graph
import tin.model.query.QueryNode
import tin.services.technical.SystemConfigurationService
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path
import java.util.HashMap

@Service
abstract class FileReaderService<T> (systemConfigurationService: SystemConfigurationService) {
    abstract var filePath: String;
    abstract var inputFileMaxLines: Int;
    var readingNodes = false;
    var readingEdges = false;
    var warnings: MutableList<FileReaderWarning> = mutableListOf();
    var errors: MutableList<FileReaderWarning> = mutableListOf();


    fun read(fileName: String) : FileReaderResult<T> {
        var absPath = Path.of(filePath).resolve(fileName);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file);
    }
    fun read(path: Path) : FileReaderResult<T> {
        var file = this.readFileFromAbsolutePath(path);
        return this.processFile(file);
    }
    fun read(dir: Path, filename: String) : FileReaderResult<T> {
        var absPath = dir.resolve(filename);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file);
    }

    fun read(dir: String, filename: String) : FileReaderResult<T>{
        var absPath = Path.of(dir).resolve(filename);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file);
    }

    protected fun readFileFromAbsolutePath(path: Path) : File {
        return path.toFile();
    }

    abstract fun processFile(file: File): FileReaderResult<T>

    protected fun warn(message: String, index: Int, line: String){
        this.warnings.add(FileReaderWarning(message, index, line))
    }

    protected fun error(message: String, index: Int, line: String){
        this.errors.add(FileReaderWarning(message, index, line))
    }

}