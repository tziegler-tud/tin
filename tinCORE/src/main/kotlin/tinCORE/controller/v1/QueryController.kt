package tinCORE.controller.v1

import OnotlogyTestPostData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinCORE.data.File.TinFileRepository
import tinCORE.data.api.query.QueryInfoData
import tinCORE.services.File.FileService
import tinCORE.services.internal.fileReaders.QueryReaderServiceV2

import java.nio.file.Path

@RestController
@RequestMapping("/api/v1/query")
class QueryController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: TinFileRepository

    @Autowired
    lateinit var queryReaderService: QueryReaderServiceV2

    @GetMapping("info")
    fun getQueryInfoAll(): List<QueryInfoData> {
        //get ontology files
        val files = fileService.getAllQueryFiles()
        val fileInfoList: MutableList<QueryInfoData> = mutableListOf();
        files.forEach { file ->
            //read file
            val path = file.path
            if(path == null) return@forEach;
            try{
                val result = queryReaderService.read(Path.of(path), false)
                fileInfoList.add(QueryInfoData(file.filename, result.graph))
            }
            catch(e: Exception){
                return@forEach;
            }
        }
        return fileInfoList
    }
}