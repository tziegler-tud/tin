package tinDL.controller.tintheweb.api.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinDL.model.v1.tintheweb.FileRepository
import tinDL.services.files.FileService
import tinDL.services.technical.SystemConfigurationService
import java.nio.file.Path

import tinLIB.services.internal.fileReaders.TransducerReaderServiceV2
import tinLIB.data.tintheweb.transducer.TransducerInfoData


@RestController
@RequestMapping("/api/v1/transducer")
class TransducerController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: FileRepository

    @Autowired
    private lateinit var systemConfigurationService: SystemConfigurationService

    val transducerReaderService: TransducerReaderServiceV2 = TransducerReaderServiceV2(
        systemConfigurationService.getTransducerPath(),
        systemConfigurationService.getTransducerSizeLimit()
    )
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