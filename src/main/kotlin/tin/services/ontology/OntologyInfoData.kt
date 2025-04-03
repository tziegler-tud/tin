package tin.services.ontology

import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLEntity

class OntologyInfoData(val ontologyIRI: String, val ontologyName: String, val aboxAxioms: Set<OWLAxiom>, val tboxAxioms: Set<OWLAxiom>, val signature: Set<OWLEntity>, val classCount: Int, val roleCount: Int, val individualCount: Int) {}

