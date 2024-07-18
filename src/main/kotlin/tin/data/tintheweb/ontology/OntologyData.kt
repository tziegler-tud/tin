package tin.data.tintheweb.ontology

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.semanticweb.owlapi.model.OWLAxiom
import org.semanticweb.owlapi.model.OWLEntity
import tin.model.queryResult.RegularPathQueryResult
import tin.services.ontology.OntologyInfoData
import tin.services.ontology.OntologyManager

data class OntologyData @JsonCreator constructor(
    @JsonProperty("filename") val fname: String,
    @JsonProperty("AboxAxiomCount") val aboxAxiomCount: Int,
    @JsonProperty("TboxAxiomCount") val tboxAxiomCount: Int,
    @JsonProperty("SignatureCount") val signatureCount: Int,
    @JsonProperty("AboxAxioms") val axboxAxioms: List<String>,
    @JsonProperty("TboxAxioms") val tboxAxions: List<String>,
    @JsonProperty("signature") val signature: List<String>,
    @JsonProperty("reasoner") val reasoner: String,
) {
    constructor(ontologyInfoData: OntologyInfoData): this(
        fname = ontologyInfoData.filename,
        aboxAxiomCount = ontologyInfoData.aboxAxioms.size,
        tboxAxiomCount = ontologyInfoData.tboxAxioms.size,
        signatureCount = ontologyInfoData.signature.size,
        axboxAxioms = ontologyInfoData.aboxAxioms.map{it.toString()},
        tboxAxions = ontologyInfoData.tboxAxioms.map{it.toString()},
        signature = ontologyInfoData.signature.map{it.toString()},
        reasoner = ontologyInfoData.getCurrentReasonerName(),

    )
}
