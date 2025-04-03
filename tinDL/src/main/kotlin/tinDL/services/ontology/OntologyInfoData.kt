package tinDL.services.ontology

import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLEntity

class OntologyInfoData(val filename: String, val aboxAxioms: Set<OWLAxiom>, val tboxAxioms: Set<OWLAxiom>, val signature: Set<OWLEntity>)

