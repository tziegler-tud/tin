package tinDL.services.internal.fileReaders

import org.springframework.stereotype.Service
import tinDL.services.technical.SystemConfigurationService
import java.io.File
import java.nio.file.Path

@Service
abstract class FileReaderService<T> (systemConfigurationService: SystemConfigurationService) {
    abstract var filePath: String;
    abstract var inputFileMaxLines: Int;
    var readingNodes = false;
    var readingEdges = false;
    var warnings: MutableList<FileReaderWarning> = mutableListOf();
    var errors: MutableList<FileReaderWarning> = mutableListOf();

    val commentLineRegex = Regex("\\s*//.*");

    final fun read(fileName: String, breakOnError: Boolean = false) : T {
        var absPath = Path.of(filePath).resolve(fileName);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file, breakOnError);
    }
    final fun read(path: Path, breakOnError: Boolean = false) : T {
        var file = this.readFileFromAbsolutePath(path);
        return this.processFile(file, breakOnError);
    }
    final fun read(dir: Path, filename: String, breakOnError: Boolean = false) : T {
        var absPath = dir.resolve(filename);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file, breakOnError);
    }

    final fun read(dir: String, filename: String, breakOnError: Boolean = false) : T {
        var absPath = Path.of(dir).resolve(filename);
        var file = this.readFileFromAbsolutePath(absPath);
        return this.processFile(file, breakOnError);
    }

    protected fun readFileFromAbsolutePath(path: Path) : File {
        return path.toFile();
    }

    abstract fun processFile(file: File, breakOnError: Boolean = false): T

    protected fun warn(message: String, index: Int, line: String){
        this.warnings.add(FileReaderWarning(message, index, line))
    }

    protected fun error(message: String, index: Int, line: String){
        this.errors.add(FileReaderWarning(message, index, line))
    }

}