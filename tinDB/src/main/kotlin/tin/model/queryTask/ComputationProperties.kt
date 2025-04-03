package tin.model.queryTask

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class ComputationProperties(
    val topKValue: Int?,
    val thresholdValue: Double?,
    val generateTransducer: Boolean,
    val transducerGeneration: TransducerGeneration?,
    val name: String,
    val computationModeEnum: ComputationModeEnum,
) {
    @GeneratedValue
    @Id
    val id: Long = 0

    @OneToMany(mappedBy = "computationProperties")
    val queryTask: List<QueryTask> = mutableListOf()

    enum class TransducerGeneration {
        ClassicalAnswersPreserving,
        EditDistance,
    }

    enum class ComputationModeEnum {
        Dijkstra,
        TopK,
        Threshold
    }
}

interface ComputationPropertiesRepository : JpaRepository<ComputationProperties, Long> {

}