package tin.controller.tintheweb

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import tin.data.tintheweb.ontology.OntologyData
import tin.services.internal.fileReaders.OntologyReaderService
import tin.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tin.services.ontology.OntologyInfoData
import tin.services.ontology.OntologyManager
import tin.services.tintheweb.ComputationPropertiesService
import java.io.File

@RestController
class OntologyTestController(
    private val computationPropertiesService: ComputationPropertiesService
) {


    @Autowired
    lateinit var ontologyReaderService: OntologyReaderService

    @PostMapping("ontology/load")
    fun loadTestOntology(): OntologyData {
        val manager = OntologyManager();
        //get ontology file
        val result: FileReaderResult<File> = ontologyReaderService.read("pizza.rdf");

        manager.loadOntology(result.get());
        manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val info: OntologyInfoData = manager.getOntologyInfo();
        return OntologyData(info)
    }

    @GetMapping("ontology/info/{filename}")
    fun getOntologyInfo(@PathVariable filename: String): OntologyData {
        val manager = OntologyManager();
        //get ontology file
        val result: FileReaderResult<File> = ontologyReaderService.read(filename);

        manager.loadOntology(result.get());
        manager.loadReasoner(OntologyManager.BuildInReasoners.HERMIT)
        val info: OntologyInfoData = manager.getOntologyInfo();
        return OntologyData(info)
    }
}