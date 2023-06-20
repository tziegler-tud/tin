package tin.controller.tintheweb

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import tin.services.tintheweb.FileOverviewService

@RestController
class FileOverviewController(
    private val fileOverviewService: FileOverviewService
) {

    @GetMapping("file-overview/regular-path-queries")
    fun getAllRegularPathQueryFiles() = fileOverviewService.getAllRegularPathQueryFiles()

    @GetMapping("file-overview/databases")
    fun getAllDatabaseFiles() = fileOverviewService.getAllDatabaseFiles()

    @GetMapping("file-overview/transducers")
    fun getAllTransducerFiles() = fileOverviewService.getAllTransducerFiles()
}