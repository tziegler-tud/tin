package tin.model.queryResult.computationStatistics

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

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