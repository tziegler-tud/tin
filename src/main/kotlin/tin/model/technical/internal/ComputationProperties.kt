package tin.model.technical.internal

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class ComputationProperties(
    val topKValue: Int?,
    val thresholdValue: Double?,
    val generateTransducer: Boolean,
    val transducerGeneration: TransducerGeneration?,

) {
    @GeneratedValue
    @Id
    val id: Long = 0

    enum class TransducerGeneration {
        ClassicalAnswersPreserving,
        EditDistance,
    }
}

interface ComputationPropertiesRepository : JpaRepository<ComputationProperties, Long>