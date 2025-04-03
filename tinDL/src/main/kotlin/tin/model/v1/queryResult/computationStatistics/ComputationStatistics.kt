package tin.model.v1.queryResult.computationStatistics

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository


@Entity
abstract class ComputationStatistics(
    open val preProcessingTimeInMs: Long,
    open val mainProcessingTimeInMs: Long,
    open val postProcessingTimeInMs: Long,
    open val combinedTimeInMs: Long,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0
}

interface ComputationStatisticsRepository : JpaRepository<ComputationStatistics, Long>