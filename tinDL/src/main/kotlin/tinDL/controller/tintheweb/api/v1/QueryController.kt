package tinDL.controller.tintheweb.api.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinDL.data.tintheweb.query.QueryInfoData
import tinDL.model.v1.tintheweb.FileRepository
import tinDL.services.files.FileService
import tinDL.services.technical.SystemConfigurationService
import java.nio.file.Path

import tinLIB.services.internal.fileReaders.QueryReaderServiceV2


@RestController
@RequestMapping("/api/v1/query")
class QueryController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    private lateinit var systemConfigurationService: SystemConfigurationService

    val queryReaderService: QueryReaderServiceV2 = QueryReaderServiceV2(
        systemConfigurationService.getQueryPath(),
        systemConfigurationService.getQuerySizeLimit()
    )

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