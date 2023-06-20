package tin.controller.tintheweb

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import tin.services.tintheweb.LocalFileSyncService

@RestController
class LocalFileSyncController(
    private val localFileSyncService: LocalFileSyncService
) {

    @GetMapping("sync/regular-path-queries")
    fun syncRegularPathQueries() = localFileSyncService.syncRegularPathQueryFiles()

    @GetMapping("sync/databases")
    fun syncDatabases() = localFileSyncService.syncDatabaseFiles()

    @GetMapping("sync/transducers")
    fun syncTransducers() = localFileSyncService.syncTransducerFiles()

}