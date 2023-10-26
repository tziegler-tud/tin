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
    var readingNodes = false;
    var readingEdges = false;


    fun read(fileName: String) : T {
        var absPath = Path.of(filePath).resolve(fileName);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file);
    }
    fun read(path: Path) : T {
        var file = this.readFileFromAbsolutePath(path);
        return this.processFile(file);
    }
    fun read(dir: Path, filename: String) : T {
        var absPath = dir.resolve(filename);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file);
    }

    fun read(dir: String, filename: String) : T{
        var absPath = Path.of(dir).resolve(filename);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file);
    }

    protected fun readFileFromAbsolutePath(path: Path) : File {
        return path.toFile();
    }

    abstract fun processFile(file: File): T

}