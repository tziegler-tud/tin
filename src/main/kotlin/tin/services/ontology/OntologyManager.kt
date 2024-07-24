package tin.services.ontology

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory

import org.semanticweb.elk.owlapi.ElkReasoner
import org.semanticweb.elk.owlapi.ElkReasonerFactory

import de.uni_stuttgart.vis.vowl.owl2vowl.Owl2Vowl
import org.semanticweb.HermiT.Reasoner as HermitReasoner
import org.semanticweb.HermiT.ReasonerFactory as HermitReasonerFactory
//import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import org.semanticweb.owlapi.model.parameters.Imports
import tin.model.alphabet.Alphabet
import java.io.File


class OntologyManager(val file: File) {

    private val manager = OWLManager.createOWLOntologyManager();
    private val ontology: OWLOntology = manager.loadOntologyFromOntologyDocument(file);
    private lateinit var reasoner: OWLReasoner;
    private var currentReasoner = BuildInReasoners.NONE;

    enum class BuildInReasoners {
        NONE, ELK, JCEL, HERMIT
    }

    init {
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
    }

    fun loadReasoner(reasonerName: BuildInReasoners): Boolean {

        val reasonerFactory: OWLReasonerFactory;
        when (reasonerName) {
            BuildInReasoners.ELK -> {
//                reasonerFactory = ElkReasonerFactory();
                //strange dependency problem with google guava 32.2.0
                //TODO: find a fix or throw out ELK support
                currentReasoner = reasonerName;
                reasonerFactory = ElkReasonerFactory();
            }
            BuildInReasoners.JCEL -> {
                //JCEL currently does not support OWLAPI 5.5, disable for now
                currentReasoner = BuildInReasoners.NONE;
//                reasonerFactory = JcelReasonerFactory();
                return false;

            }
            BuildInReasoners.HERMIT -> {
                currentReasoner = reasonerName;
                reasonerFactory = HermitReasonerFactory();

            }
            else -> {
                currentReasoner = BuildInReasoners.NONE;
                println("Failed to load reasoner: Unknown reasoner name given.");
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

    fun getAlphabet(): Alphabet {
        //TODO: Implement
        return Alphabet();
    }

    private fun saveToJson(filename: String, jsonString: String){
        val f = File("src/main/resources/vowl/$filename.json")
        f.createNewFile()
        f.writeText(jsonString)
    }
}