package tin.services.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tin.model.technical.QueryTask
import tin.model.technical.QueryTaskRepository
import tin.services.internal.DijkstraQueryAnsweringService

@Service
class SchedulerService(
    // Repositories
    private val queryTaskRepository: QueryTaskRepository
) {
    @Autowired
    private lateinit var dijkstraQueryAnsweringService: DijkstraQueryAnsweringService

    // every 5sec, check for a scheduled queryTask to process
    @Scheduled(cron = "0/5 * * * * *")
    fun checkForQueryTask() {
        // get the oldest queryTask
        val nextQueryTask = queryTaskRepository.findFirstByQueryStatusOrderByCreatedAtAsc(QueryTask.QueryStatus.Queued)
        // delegate it to the queryAnsweringService
        nextQueryTask?.let { dijkstraQueryAnsweringService.calculateQueryTask(nextQueryTask) }
    }
}