package tinDL.data.tintheweb.ontology

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.services.ontology.OntologyInfoData

data class OntologyData @JsonCreator constructor(
    @JsonProperty("ontologyIRI") val ontologyIRI: String,
    @JsonProperty("AboxAxiomCount") val aboxAxiomCount: Int,
    @JsonProperty("TboxAxiomCount") val tboxAxiomCount: Int,
    @JsonProperty("SignatureCount") val signatureCount: Int,
    @JsonProperty("AboxAxioms") val axboxAxioms: List<String>,
    @JsonProperty("TboxAxioms") val tboxAxions: List<String>,
    @JsonProperty("signature") val signature: List<String>,
) {
    constructor(ontologyInfoData: OntologyInfoData): this(
        ontologyIRI = ontologyInfoData.ontologyIRI,
        aboxAxiomCount = ontologyInfoData.aboxAxioms.size,
        tboxAxiomCount = ontologyInfoData.tboxAxioms.size,
        signatureCount = ontologyInfoData.signature.size,
        axboxAxioms = ontologyInfoData.aboxAxioms.map{it.toString()},
        tboxAxions = ontologyInfoData.tboxAxioms.map{it.toString()},
        signature = ontologyInfoData.signature.map{it.toString()},
    )
}
