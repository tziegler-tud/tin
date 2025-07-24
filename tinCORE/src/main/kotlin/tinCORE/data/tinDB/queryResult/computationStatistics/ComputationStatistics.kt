package tinCORE.data.tinDB.queryResult.computationStatistics

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository


@Entity
abstract class ComputationStatistics(
    open val preProcessingTimeInMs: Long = 0,
    open val mainProcessingTimeInMs: Long = 0,
    open val postProcessingTimeInMs: Long = 0,
    open val combinedTimeInMs: Long = 0,
) {

    @GeneratedValue
    @Id
    open val id: Long = 0
}

interface ComputationStatisticsRepository : JpaRepository<ComputationStatistics, Long>