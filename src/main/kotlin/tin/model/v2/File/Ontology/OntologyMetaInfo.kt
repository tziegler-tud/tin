package tin.model.v2.File.Ontology

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import tin.model.v2.File.TinFile
import tin.model.v2.Tasks.OntologyVariant
import tin.services.ontology.OntologyInfoData
import java.util.*

@Entity
class OntologyMetaInfo(

    @OneToOne(cascade = [CascadeType.ALL])
    val ontologyFileIdentifier: TinFile = TinFile(),
    val ontologyIRI: String = "",
    val ontologyShortName: String = "",
    val ontologyVariant: OntologyVariant = OntologyVariant.UNSET,
    val classCount: Int = 0,
    val roleCount: Int = 0,
    val individualCount: Int = 0,
    val tboxSize: Int = 0,
    val aboxSize: Int = 0,
    val lastModifiedAt: Date? = null) {

    constructor(ontologyFileIdentifier: TinFile, ontologyInfoData: OntologyInfoData, ontologyVariant: OntologyVariant?) : this(
        ontologyFileIdentifier = ontologyFileIdentifier,
        ontologyIRI = ontologyInfoData.ontologyIRI,
        ontologyShortName = ontologyInfoData.ontologyName,
        ontologyVariant = ontologyVariant ?: OntologyVariant.UNSET,
        classCount = ontologyInfoData.classCount,
        roleCount = ontologyInfoData.roleCount,
        individualCount = ontologyInfoData.individualCount,
        tboxSize = ontologyInfoData.tboxAxioms.size,
        aboxSize = ontologyInfoData.aboxAxioms.size,
    )
    @GeneratedValue
    @Id
    val id: Long = 0
}


interface OntologyMetaInfoRepository : JpaRepository<OntologyMetaInfo, Long>