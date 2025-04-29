//package tinCORE.controller
//
//import OnotlogyTestPostData
//import org.semanticweb.owlapi.reasoner.OWLReasoner
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.web.bind.annotation.*
//import tinDL.data.tintheweb.ontology.OntologyData
//import tinDL.services.internal.fileReaders.OntologyReaderService
//import tinDL.services.internal.fileReaders.fileReaderResult.FileReaderResult
//import tinDL.services.ontology.OntologyInfoData
//import tinDL.services.ontology.OntologyManager
//import java.io.File
//
//@RestController
//class OntologyTestController(
//) {
//
//
//    @Autowired
//    lateinit var ontologyReaderService: OntologyReaderService
//
//    @PostMapping("ontology/load")
//    fun loadTestOntology(@RequestBody data: OnotlogyTestPostData): OntologyData {
//        //parse reasoner name
//        val reasonerName = OntologyManager.BuildInReasoners.valueOf(data.reasonerName);
//
//        //get ontology file
//        val result: FileReaderResult<File> = ontologyReaderService.read(data.filename);
//
//        val manager = OntologyManager(result.get());
//        val reasoner = manager.createReasoner(reasonerName)
//        val info: OntologyInfoData = manager.getOntologyInfo();
//        return OntologyData(info)
//    }
//
//    @GetMapping("ontology/info/{filename}")
//    fun getOntologyInfo(@PathVariable filename: String): OntologyData {
//        //get ontology file
//        val result: FileReaderResult<File> = ontologyReaderService.read(filename);
//
//        val manager = OntologyManager(result.get());
//        manager.createReasoner(OntologyManager.BuildInReasoners.HERMIT)
//        val info: OntologyInfoData = manager.getOntologyInfo();
//        return OntologyData(info)
//    }
//}