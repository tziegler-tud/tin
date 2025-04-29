package tinCORE.controller.v1

import OnotlogyTestPostData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinCORE.data.File.OntologyMetaInfoRepository
import tinCORE.data.tintheweb.ontology.OntologyData
import tinCORE.data.tintheweb.ontology.OntologyMetaInfoData
import tinCORE.services.File.FileService
import tinCORE.services.internal.fileReaders.OntologyReaderService
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinDL.services.ontology.OntologyInfoData

import tinDL.services.ontology.OntologyManager
import java.io.File

@RestController
@RequestMapping("/api/v1/ontology")
class OntologyController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var ontologyMetaInfoRepository: OntologyMetaInfoRepository

    @Autowired
    lateinit var ontologyReaderService: OntologyReaderService

    @PostMapping("load")
    fun loadTestOntology(@RequestBody data: OnotlogyTestPostData): OntologyData {
        //get ontology file
        val result: FileReaderResult<File> = ontologyReaderService.read(data.filename);

        val manager = OntologyManager(result.get());
        val info: OntologyInfoData = manager.getOntologyInfo();
        return OntologyData(info)
    }

    @GetMapping("info")
    fun getOntologyInfoAll(): List<OntologyMetaInfoData> {
        //get ontology files
        val info = ontologyMetaInfoRepository.findAll().map(::OntologyMetaInfoData)
        return info
    }

    @GetMapping("info/{filename}")
    fun getOntologyInfo(@PathVariable filename: String): OntologyData {
        //get ontology file
        val result: FileReaderResult<File> = ontologyReaderService.read(filename);

        val manager = OntologyManager(result.get());
        val info: OntologyInfoData = manager.getOntologyInfo();
        return OntologyData(info)
    }
}