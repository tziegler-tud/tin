package tin.model.technical.internal

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.technical.QueryTask
import javax.persistence.*

@Entity
class ComputationMode(
    val computationModeEnum: ComputationModeEnum,

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
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
