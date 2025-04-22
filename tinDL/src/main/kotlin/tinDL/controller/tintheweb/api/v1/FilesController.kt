//package tinDL.controller.tintheweb.api.v1
//
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//import tinDL.data.tintheweb.FileData
//import tinDL.data.tintheweb.TinFileData
//import tinDL.services.files.FileService
//
//@RestController
//@RequestMapping("/api/v1/files")
//class FilesController(
//    private val fileService: FileService
//) {
//    @GetMapping("/sync")
//    fun syncLocalFiles(): Boolean {
//        fileService.syncLocalFiles();
//        return true
//    }
//
//    @GetMapping("/queries")
//    fun getAllRegularPathQueryFiles(): List<TinFileData> {
//        return fileService.getAllQueryFiles().map(::TinFileData);
//    }
//
//    @GetMapping("/transducers")
//    fun getAllDatabaseFiles(): List<TinFileData> {
//        return fileService.getAllTransducerFiles().map(::TinFileData)
//    }
//
//    @GetMapping("/ontologies")
//    fun getAllTransducerFiles(): List<TinFileData> {
//        return fileService.getAllOntologyFiles().map(::TinFileData)
//    }
//}