package tin.model.queryTask

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
class ComputationMode(
    val computationModeEnum: ComputationModeEnum,

    @OneToOne(cascade = [CascadeType.ALL])
    val computationProperties: ComputationProperties,
) {
    @GeneratedValue
    @Id
    val id: Long = 0

    enum class ComputationModeEnum{
        Dijkstra,
        TopK,
        Threshold,
    }
}

interface ComputationModeRepository: JpaRepository<ComputationMode, Long>
