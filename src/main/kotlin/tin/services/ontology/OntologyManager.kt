package tin.services.ontology

//import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory
import org.semanticweb.owlapi.util.InferredAxiomGenerator
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator

import de.uni_stuttgart.vis.vowl.owl2vowl.Owl2Vowl
import org.semanticweb.HermiT.Reasoner as HermitReasoner
import org.semanticweb.HermiT.ReasonerFactory as HermitReasonerFactory
import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import org.semanticweb.owlapi.model.parameters.Imports
import java.io.File


class OntologyManager() {

    private val manager = OWLManager.createOWLOntologyManager();
    private lateinit var ontology: OWLOntology;
    private lateinit var reasoner: OWLReasoner;
    private var currentReasoner = BuildInReasoners.NONE;

    enum class BuildInReasoners {
        NONE, ELK, JCEL, HERMIT

    }

    fun loadOntology(path: String){

    }

    fun loadOntology(file: File): OWLOntology {
        ontology = manager.loadOntologyFromOntologyDocument(file);
        //extract some ontology insights for testing
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);

        println("Abox axioms: $aboxAxioms");
        println("Tbox axioms: $tboxAxioms");
        println("Signature: $signature");

        //create json represantation for vowl
        val vowlJson = Owl2Vowl(ontology).getJsonAsString();
        //save to json file
        saveToJson(file.getName(), vowlJson)

        return ontology;
    }

    fun loadReasoner(reasonerName: BuildInReasoners): Boolean {

        if(!this::ontology.isInitialized){
            print("Failed to load reasoner: No ontology is loaded.");
            return false;
        }
        val reasonerFactory: OWLReasonerFactory;
        when (reasonerName) {
            BuildInReasoners.ELK -> {
//                reasonerFactory = ElkReasonerFactory();
                //strange dependency problem with google guava 32.2.0
                //TODO: find a fix or throw out ELK support
                currentReasoner = BuildInReasoners.NONE;
                return false;
            }
            BuildInReasoners.JCEL -> {
                currentReasoner = reasonerName;
                reasonerFactory = JcelReasonerFactory();

            }
            BuildInReasoners.HERMIT -> {
                currentReasoner = reasonerName;
                reasonerFactory = HermitReasonerFactory();

            }
            else -> {
                currentReasoner = BuildInReasoners.NONE;
                print("Failed to load reasoner: Unknown reasoner name given.");
                return false;
            }
        }
        reasoner = reasonerFactory.createReasoner(ontology)
//
//        // Classify the ontology.
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

        // To generate an inferred ontology we use implementations of
        // inferred axiom generators
//        val gens: MutableList<InferredAxiomGenerator<out OWLAxiom?>> = ArrayList()
//        gens.add(InferredSubClassAxiomGenerator())
//        gens.add(InferredEquivalentClassAxiomGenerator())
        return true;
    }

    fun getOntologyInfo(): OntologyInfoData {
        val name = manager.getOntologyDocumentIRI(ontology);
        val aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);
        val tboxAxioms = ontology.getTBoxAxioms(Imports.EXCLUDED);
        val signature = ontology.getSignature(Imports.EXCLUDED);
        val reasoner = currentReasoner;

        return OntologyInfoData(name.toString(), aboxAxioms, tboxAxioms, signature, reasoner);
    }

    private fun saveToJson(filename: String, jsonString: String){
        val f = File("src/main/resources/vowl/$filename.json")
        f.createNewFile()
        f.writeText(jsonString)
    }
}