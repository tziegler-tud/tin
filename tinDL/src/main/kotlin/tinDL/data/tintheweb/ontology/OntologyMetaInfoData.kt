package tinDL.data.tintheweb.ontology

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tinDL.model.v2.File.Ontology.OntologyMetaInfo
import tinDL.services.ontology.OntologyInfoData
import java.util.*

data class OntologyMetaInfoData @JsonCreator constructor(
    @JsonProperty("fileIdentifier") val fileId: Long,
    @JsonProperty("ontologyIRI") val ontologyIRI: String,
    @JsonProperty("ontologyShortName") val ontologyShortName: String,
    @JsonProperty("ontologyVariant") val variant: String,
    @JsonProperty("classCount") val classCount: Int,
    @JsonProperty("roleCount") val roleCount: Int,
    @JsonProperty("individualCount") val individualCount: Int,
    @JsonProperty("tboxSize") val tboxSize: Int,
    @JsonProperty("aboxSize") val aboxSize: Int,
    @JsonProperty("lastModifiedAt") val lastModified: Date?,
) {
    constructor(ontologyMetaInfo: OntologyMetaInfo): this(
        fileId = ontologyMetaInfo.ontologyFileIdentifier.id,
        ontologyIRI = ontologyMetaInfo.ontologyIRI,
        ontologyShortName = ontologyMetaInfo.ontologyShortName,
        variant = ontologyMetaInfo.ontologyVariant.name,
        classCount = ontologyMetaInfo.classCount,
        roleCount = ontologyMetaInfo.roleCount,
        individualCount = ontologyMetaInfo.individualCount,
        tboxSize = ontologyMetaInfo.tboxSize,
        aboxSize = ontologyMetaInfo.aboxSize,
        lastModified = ontologyMetaInfo.lastModifiedAt
    )
}
