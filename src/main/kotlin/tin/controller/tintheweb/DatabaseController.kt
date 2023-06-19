package tin.controller.tintheweb

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import tin.data.tintheweb.FileCompleteData
import tin.services.DatabaseService

@RestController
class DatabaseController(
    private val databaseService: DatabaseService
) {
    @PostMapping("/databases/upload")
    fun uploadDatabaseFile(@RequestParam("file") file: MultipartFile) {
        println("uploaded")
        databaseService.uploadDatabaseFile(file)
    }

    @GetMapping("/databases")
    fun getAllDatabaseMetaData() = databaseService.getAllDatabaseMetaData().map { FileCompleteData(it)}

    @GetMapping("/databases/test")
    fun test() = print("test")

}