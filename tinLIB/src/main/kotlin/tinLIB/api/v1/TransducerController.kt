package tinLIB.api.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinLIB.data.File.TinFileRepository
import tinLIB.data.api.transducer.TransducerInfoData
import tinLIB.services.files.FileService
import tinLIB.services.internal.fileReaders.TransducerReaderServiceV2
import tinLIB.services.technical.SystemConfigurationService
import java.nio.file.Path

@RestController
@RequestMapping("/api/v1/transducer")
class TransducerController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: TinFileRepository

    @Autowired
    private lateinit var systemConfigurationService: SystemConfigurationService

    var transducerReaderService = TransducerReaderServiceV2(systemConfigurationService)

    @GetMapping("info")
    fun getQueryInfoAll(): List<TransducerInfoData> {
        //get ontology files
        val files = fileService.getAllTransducerFiles()
        val fileInfoList: MutableList<TransducerInfoData> = mutableListOf();
        files.forEach { file ->
            //read file
            val path = file.path
            if(path == null) return@forEach;
            try{
                val result = transducerReaderService.read(Path.of(path), false)
                fileInfoList.add(TransducerInfoData(file.filename, result.graph))
            }
            catch(e: Exception){
                return@forEach;
            }
        }
        return fileInfoList
    }
}