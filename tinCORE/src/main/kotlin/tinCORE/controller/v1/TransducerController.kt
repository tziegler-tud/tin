package tinCORE.controller.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinCORE.data.File.TinFileRepository
import tinCORE.data.api.transducer.TransducerInfoData
import tinCORE.services.File.FileService
import tinCORE.services.internal.fileReaders.TransducerReaderServiceV2
import java.nio.file.Path

@RestController
@RequestMapping("/api/v1/transducer")
class TransducerController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: TinFileRepository

    @Autowired
    lateinit var transducerReaderService: TransducerReaderServiceV2

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