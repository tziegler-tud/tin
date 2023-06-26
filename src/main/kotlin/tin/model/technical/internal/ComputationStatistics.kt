package tin.model.technical.internal

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.technical.QueryResult
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class ComputationStatistics(
    val preProcessingTimeInMs: Long,
    val mainProcessingTimeInMs: Long,
    val postProcessingTimeInMs: Long,
    @OneToOne(mappedBy = "computationStatistics")
    val queryResult: QueryResult,
) {

    @GeneratedValue
    @Id
    val id: Long = 0
}

interface ComputationStatisticsRepository : JpaRepository<ComputationStatistics, Long>