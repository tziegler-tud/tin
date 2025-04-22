//package tinDL.controller.tintheweb.api.v1
//
//import OnotlogyTestPostData
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.web.bind.annotation.*
//import tinDL.data.tintheweb.TinFileData
//import tinDL.data.tintheweb.ontology.OntologyData
//import tinDL.data.tintheweb.ontology.OntologyMetaInfoData
//import tinDL.data.tintheweb.query.QueryInfoData
//import tinDL.model.v1.tintheweb.FileRepository
//import tinDL.model.v2.File.Ontology.OntologyMetaInfo
//import tinDL.model.v2.File.Ontology.OntologyMetaInfoRepository
//import tinDL.services.files.FileService
//import tinDL.services.internal.fileReaders.OntologyReaderService
//import tinDL.services.internal.fileReaders.QueryReaderServiceV2
//import tinDL.services.internal.fileReaders.fileReaderResult.FileReaderResult
//import tinDL.services.ontology.OntologyInfoData
//import tinDL.services.ontology.OntologyManager
//import java.io.File
//import java.nio.file.Path
//
//@RestController
//@RequestMapping("/api/v1/query")
//class QueryController(
//    private val fileService: FileService
//) {
//    @Autowired
//    private lateinit var fileRepository: FileRepository
//
//    @Autowired
//    lateinit var queryReaderService: QueryReaderServiceV2
//
//    @GetMapping("info")
//    fun getQueryInfoAll(): List<QueryInfoData> {
//        //get ontology files
//        val files = fileService.getAllQueryFiles()
//        val fileInfoList: MutableList<QueryInfoData> = mutableListOf();
//        files.forEach { file ->
//            //read file
//            val path = file.path
//            if(path == null) return@forEach;
//            try{
//                val result = queryReaderService.read(Path.of(path), false)
//                fileInfoList.add(QueryInfoData(file.filename, result.graph))
//            }
//            catch(e: Exception){
//                return@forEach;
//            }
//        }
//        return fileInfoList
//    }
//}