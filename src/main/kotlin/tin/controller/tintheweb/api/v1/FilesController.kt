package tin.controller.tintheweb.api.v1

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tin.data.tintheweb.FileData
import tin.services.tintheweb.FileOverviewService

@RestController
@RequestMapping("/api/v1/files")
class FilesController(
    private val fileOverviewService: FileOverviewService
) {

    @GetMapping("/queries")
    fun getAllRegularPathQueryFiles(): List<FileData> {
        return fileOverviewService.getAllRegularPathQueryFiles().map(::FileData)
    }

    @GetMapping("/transducers")
    fun getAllDatabaseFiles(): List<FileData> {
        return fileOverviewService.getAllTransducerFiles().map(::FileData)
    }

    @GetMapping("/ontologies")
    fun getAllTransducerFiles(): List<FileData> {
        return fileOverviewService.getAllOntologyFiles().map(::FileData)
    }
}