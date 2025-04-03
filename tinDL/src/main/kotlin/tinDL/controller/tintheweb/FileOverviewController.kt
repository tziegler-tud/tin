package tinDL.controller.tintheweb

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import tinDL.data.tintheweb.FileData
import tinDL.services.tintheweb.FileOverviewService

@RestController
class FileOverviewController(
    private val fileOverviewService: FileOverviewService
) {

    @GetMapping("file-overview/regular-path-queries")
    fun getAllRegularPathQueryFiles(): List<FileData> {
        return fileOverviewService.getAllRegularPathQueryFiles().map(::FileData)
    }

    @GetMapping("file-overview/conjunctive-path-queries")
    fun getAllConjunctivePathQueryFiles(): List<FileData> {
        return fileOverviewService.getAllConjunctivePathQueryFiles().map(::FileData)
    }

    @GetMapping("file-overview/databases")
    fun getAllDatabaseFiles(): List<FileData> {
        return fileOverviewService.getAllDatabaseFiles().map(::FileData)
    }

    @GetMapping("file-overview/transducers")
    fun getAllTransducerFiles(): List<FileData> {
        return fileOverviewService.getAllTransducerFiles().map(::FileData)
    }
}