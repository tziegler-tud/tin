package tinDL.services.ontology

import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLEntity

class OntologyInfoData(
    val ontologyIRI: String,
    val ontologyName: String,
    aboxAxioms: Set<OWLAxiom>,
    tboxAxioms: Set<OWLAxiom>,
    signature: Set<OWLEntity>,
    val classCount: Int,
    val roleCount: Int,
    val individualCount: Int
) {
    val aboxAxioms: List<String> = aboxAxioms.map{it.toString()}
    val tboxAxioms: List<String> = tboxAxioms.map{it.toString()}
    val signature: List<String> = signature.map{it.toString()}
}

