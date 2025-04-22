package tinLIB.api.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinLIB.data.api.query.QueryInfoData
import tinLIB.data.File.TinFile
import tinLIB.data.File.TinFileRepository
import tinLIB.services.files.FileService
import tinLIB.services.internal.fileReaders.QueryReaderServiceV2
import tinLIB.services.technical.SystemConfigurationService
import java.nio.file.Path

@RestController
@RequestMapping("/api/v1/query")
class QueryController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: TinFileRepository

    @Autowired
    private lateinit var systemConfigurationService: SystemConfigurationService

    var queryReaderService = QueryReaderServiceV2(systemConfigurationService)

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