package tinDL.controller.tintheweb.api.v1

import OnotlogyTestPostData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tinDL.data.tintheweb.ontology.OntologyData
import tinDL.data.tintheweb.ontology.OntologyMetaInfoData
import tinDL.model.v2.File.Ontology.OntologyMetaInfo
import tinDL.model.v2.File.Ontology.OntologyMetaInfoRepository
import tinDL.services.files.FileService
import tinDL.services.ontology.OntologyInfoData
import tinDL.services.ontology.OntologyManager
import tinDL.services.technical.SystemConfigurationService

import tinDL.data.tintheweb.TinFileData

import tinLIB.services.internal.fileReaders.OntologyReaderService
import tinLIB.services.internal.fileReaders.fileReaderResult.FileReaderResult

import java.io.File

@RestController
@RequestMapping("/api/v1/ontology")
class OntologyController(
    private val fileService: FileService
) {
    @Autowired
    private lateinit var ontologyMetaInfoRepository: OntologyMetaInfoRepository

    @Autowired
    private lateinit var systemConfigurationService: SystemConfigurationService

    private val ontologyReaderService: OntologyReaderService = OntologyReaderService(
        systemConfigurationService.getOntologyPath()
    )

    @PostMapping("load")
    fun loadTestOntology(@RequestBody data: OnotlogyTestPostData): OntologyData {
        //parse reasoner name
        val reasonerName = OntologyManager.BuildInReasoners.valueOf(data.reasonerName);

        //get ontology file
        val result: FileReaderResult<File> = ontologyReaderService.read(data.filename);

        val manager = OntologyManager(result.get());
        val reasoner = manager.createReasoner(reasonerName)
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
        manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val info: OntologyInfoData = manager.getOntologyInfo();
        return OntologyData(info)
    }
}