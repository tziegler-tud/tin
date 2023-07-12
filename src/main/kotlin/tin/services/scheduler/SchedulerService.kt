package tin.services.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tin.model.queryTask.QueryTask
import tin.model.queryTask.QueryTaskRepository
import tin.services.internal.DijkstraQueryAnsweringService

@Service
class SchedulerService(
    // Repositories
    private val queryTaskRepository: QueryTaskRepository
) {
    @Autowired
    private lateinit var dijkstraQueryAnsweringService: DijkstraQueryAnsweringService

    // every 5sec, check for a scheduled queryTask to process
    //@Scheduled(cron = "0/5 * * * * *")

    // for debugging - delay further executions by one year
    @Scheduled(initialDelay = 1000L, fixedDelay = 31536000000L)
    fun checkForQueryTask() {
        println("check for query task")
        // get the oldest queryTask
        val nextQueryTask = queryTaskRepository.findFirstByQueryStatusOrderByCreatedAtAsc(QueryTask.QueryStatus.Queued)
        // delegate it to the queryAnsweringService
        nextQueryTask?.let { val queryResult = dijkstraQueryAnsweringService.calculateQueryTask(nextQueryTask) }
    }
}