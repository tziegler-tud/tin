package tin.controller.tintheweb

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import tin.services.QueryService

@RestController
@CrossOrigin("*")
//TODO: delete after QueryService merge
class FileUploadController(
    private val fileUploadService: QueryService
) {

    @PostMapping("upload/query")
    fun uploadQueryFile(@RequestParam("file") file: MultipartFile){
        fileUploadService.uploadQueryFile(file)
    }



}