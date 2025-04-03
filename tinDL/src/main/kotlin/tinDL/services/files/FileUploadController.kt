package tinDL.services.files

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import tin.model.v2.File.TinFileType
import tin.model.v2.File.TinFile
import tin.model.v2.Tasks.OntologyVariant
import tin.services.files.FileService
import tin.services.files.StorageFileNotFoundException
import tin.services.technical.SystemConfigurationService
import tinDL.model.v2.File.TinFileType
import tinDL.model.v2.File.TinFile
import tinDL.services.technical.SystemConfigurationService
import java.nio.file.Path

@RestController
class FileUploadController @Autowired constructor(private val systemConfigurationService: SystemConfigurationService, private val fileService: FileService) {

    private val queryPath = Path.of(systemConfigurationService.getUploadQueryPath());
    private val transducerPath = Path.of(systemConfigurationService.getUploadTransducerPath());
    private val ontologyPath = Path.of(systemConfigurationService.getUploadOntologyPath());

    @GetMapping("/files/get/{fileId:.+}")
    @ResponseBody
    fun serveFile(@PathVariable fileId: Long?): ResponseEntity<Resource> {
        if(fileId == null) return ResponseEntity.notFound().build()
        val file: TinFile = fileService.getFile(fileId) ?: return ResponseEntity.notFound().build();
        val resource: Resource = fileService.loadAsResource(file)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.filename + "\""
        ).body(resource)
    }

//    @GetMapping("/files/upload/query/{fileId:.+}")
//    @ResponseBody
//    fun serveQueryFile(@PathVariable fileId: Long?): ResponseEntity<Resource> {
//        if(fileId == null) return ResponseEntity.notFound().build()
//        val file: TinFile = fileService.getFile(fileId) ?: return ResponseEntity.notFound().build();
//        val resource: Resource = fileService.loadAsResource(file)
//            ?: return ResponseEntity.notFound().build()
//
//        return ResponseEntity.ok().header(
//            HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=\"" + file.filename + "\""
//        ).body(resource)
//    }
//
//    @GetMapping("/files/upload/transducer/{filename:.+}")
//    @ResponseBody
//    fun serveTransducerFile(@PathVariable filename: String?): ResponseEntity<Resource> {
//        val file: Resource = storageService.loadTransducerFile(filename)
//            ?: return ResponseEntity.notFound().build()
//
//        return ResponseEntity.ok().header(
//            HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=\"" + file.filename + "\""
//        ).body(file)
//    }
//
//    @GetMapping("/files/upload/ontology/{filename:.+}")
//    @ResponseBody
//    fun serveOntologyFile(@PathVariable filename: String?): ResponseEntity<Resource> {
//        val file: Resource = storageService.loadOntologyFile(filename)
//            ?: return ResponseEntity.notFound().build()
//
//        return ResponseEntity.ok().header(
//            HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=\"" + file.filename + "\""
//        ).body(file)
//    }

    @PostMapping("/files/upload/query")
    fun handleFileUploadQuery(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        fileService.addFile(file, TinFileType.RegularPathQuery)
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
        fileService.addFile(file, TinFileType.Transducer)
        redirectAttributes.addFlashAttribute(
            "message",
            "You successfully uploaded " + file.originalFilename + "!"
        )

        return "redirect:/"
    }

    @PostMapping("/files/upload/ontology")
    fun handleFileUploadOntology(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("variant") ontologyVariant: OntologyVariant,
        redirectAttributes: RedirectAttributes
    ): String {
        fileService.addFile(file, TinFileType.Ontology, ontologyVariant)
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