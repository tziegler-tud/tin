package tin.controller.tintheweb.api.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tin.data.tintheweb.query.QueryInfoData
import tin.data.tintheweb.transducer.TransducerInfoData
import tin.model.v1.tintheweb.FileRepository
import tin.services.files.FileService
import tin.services.internal.fileReaders.QueryReaderServiceV2
import tin.services.internal.fileReaders.TransducerReaderServiceV2
import java.nio.file.Path

@RestController
@RequestMapping("/api/v1/transducer")
class TransducerController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var fileRepository: FileRepository

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