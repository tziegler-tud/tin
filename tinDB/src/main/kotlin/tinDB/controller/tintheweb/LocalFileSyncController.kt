package tinDB.controller.tintheweb

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import tinDB.services.tintheweb.LocalFileSyncService

@RestController
class LocalFileSyncController(
    private val localFileSyncService: LocalFileSyncService
) {

    @GetMapping("sync/regular-path-queries")
    fun syncRegularPathQueries() = localFileSyncService.syncRegularPathQueryFiles()

    @GetMapping("sync/conjunctive-path-queries")
    fun syncConjunctivePathQueries() = localFileSyncService.syncConjunctivePathQueryFiles()

    @GetMapping("sync/databases")
    fun syncDatabases() = localFileSyncService.syncDatabaseFiles()

    @GetMapping("sync/transducers")
    fun syncTransducers() = localFileSyncService.syncTransducerFiles()

}