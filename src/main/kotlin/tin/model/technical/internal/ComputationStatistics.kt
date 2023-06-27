package tin.model.technical.internal

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class ComputationStatistics(
    val preProcessingTimeInMs: Long,
    val mainProcessingTimeInMs: Long,
    val postProcessingTimeInMs: Long,
) {

    @GeneratedValue
    @Id
    val id: Long = 0
}

interface ComputationStatisticsRepository : JpaRepository<ComputationStatistics, Long>