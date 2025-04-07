package tinLIB.services.internal.fileReaders

import java.io.File
import java.nio.file.Path

abstract class FileReaderService<T> (
    protected var filePath: String,
) {
    private var readingNodes = false
    private var readingEdges = false
    protected var warnings: MutableList<FileReaderWarning> = mutableListOf()
    protected var errors: MutableList<FileReaderWarning> = mutableListOf()

    val commentLineRegex = Regex("\\s*//.*")

    fun read(fileName: String, breakOnError: Boolean = false) : T {
        val absPath = Path.of(filePath).resolve(fileName)
        val file = this.readFileFromAbsolutePath(absPath)
        return this.processFile(file, breakOnError)
    }
    fun read(path: Path, breakOnError: Boolean = false) : T {
        val file = this.readFileFromAbsolutePath(path)
        return this.processFile(file, breakOnError)
    }
    fun read(dir: Path, filename: String, breakOnError: Boolean = false) : T {
        val absPath = dir.resolve(filename)
        val file = this.readFileFromAbsolutePath(absPath)
        return this.processFile(file, breakOnError)
    }

    fun read(dir: String, filename: String, breakOnError: Boolean = false) : T {
        val absPath = Path.of(dir).resolve(filename)
        val file = this.readFileFromAbsolutePath(absPath)
        return this.processFile(file, breakOnError)
    }

    private fun readFileFromAbsolutePath(path: Path) : File {
        return path.toFile()
    }

    abstract fun processFile(file: File, breakOnError: Boolean = false): T

    protected fun warn(message: String, index: Int, line: String){
        this.warnings.add(FileReaderWarning(message, index, line))
    }

    protected fun error(message: String, index: Int, line: String){
        this.errors.add(FileReaderWarning(message, index, line))
    }

}