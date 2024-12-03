package tin.services.files

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import tin.services.technical.SystemConfigurationService

import java.io.IOException
import java.nio.file.Path

@Controller
class FileUploadController @Autowired constructor(private val storageService: FileSystemStorageService, private val systemConfigurationService: SystemConfigurationService) {

    private val queryPath = Path.of(systemConfigurationService.getUploadQueryPath());
    private val transducerPath = Path.of(systemConfigurationService.getUploadTransducerPath());
    private val ontologyPath = Path.of(systemConfigurationService.getUploadOntologyPath());

    @GetMapping("/files/upload/query/{filename:.+}")
    @ResponseBody
    fun serveQueryFile(@PathVariable filename: String?): ResponseEntity<Resource> {
        val file: Resource = storageService.loadQueryFile(filename)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.filename + "\""
        ).body(file)
    }

    @GetMapping("/files/upload/transducer/{filename:.+}")
    @ResponseBody
    fun serveTransducerFile(@PathVariable filename: String?): ResponseEntity<Resource> {
        val file: Resource = storageService.loadTransducerFile(filename)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.filename + "\""
        ).body(file)
    }

    @GetMapping("/files/upload/ontology/{filename:.+}")
    @ResponseBody
    fun serveOntologyFile(@PathVariable filename: String?): ResponseEntity<Resource> {
        val file: Resource = storageService.loadOntologyFile(filename)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.filename + "\""
        ).body(file)
    }

    @PostMapping("/files/upload/query")
    fun handleFileUploadQuery(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        storageService.storeQueryFile(file)
        redirectAttributes.addFlashAttribute(
            "message",
            "You successfully uploaded " + file.originalFilename + "!"
        )

        return "redirect:/"
    }

    @PostMapping("/files/upload/transducer")
    fun handleFileUploadTransducer(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        storageService.storeTransducerFile(file)
        redirectAttributes.addFlashAttribute(
            "message",
            "You successfully uploaded " + file.originalFilename + "!"
        )

        return "redirect:/"
    }

    @PostMapping("/files/upload/ontology")
    fun handleFileUploadOntology(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        storageService.storeOntologyFile(file)
        redirectAttributes.addFlashAttribute(
            "message",
            "You successfully uploaded " + file.originalFilename + "!"
        )

        return "redirect:/"
    }

    @ExceptionHandler(StorageFileNotFoundException::class)
    fun handleStorageFileNotFound(exc: StorageFileNotFoundException?): ResponseEntity<*> {
        return ResponseEntity.notFound().build<Any>()
    }
}