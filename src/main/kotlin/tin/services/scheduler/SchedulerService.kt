package tin.services.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tin.model.queryTask.QueryTask
import tin.model.queryTask.QueryTaskRepository
import tin.services.internal.queryAnswering.ConjunctiveQueryAnsweringService
import tin.services.internal.queryAnswering.RegularPathQueryAnsweringService

@Service
class SchedulerService(
    // Repositories
    private val queryTaskRepository: QueryTaskRepository
) {
    @Autowired
    private lateinit var regularPathQueryAnsweringService: RegularPathQueryAnsweringService

    @Autowired
    private lateinit var conjunctiveQueryAnsweringService: ConjunctiveQueryAnsweringService


    // for debugging - delay further executions by one year
    //@Scheduled(initialDelay = 1000L, fixedDelay = 31536000000L)


    // every 5sec, check for a scheduled queryTask to process
    @Scheduled(cron = "0/5 * * * * *")
    @Transactional
    fun checkForQueryTask() {
        // get the oldest queryTask
        val nextQueryTask = queryTaskRepository.findFirstByQueryStatusOrderByCreatedAtAsc(QueryTask.QueryStatus.Queued)

        // delegate it to the queryAnsweringService
        nextQueryTask?.let {
            when (it.queryType) {
                QueryTask.QueryType.regularPathQuery -> regularPathQueryAnsweringService.calculateQueryTask(it)
                QueryTask.QueryType.conjunctiveQuery -> conjunctiveQueryAnsweringService.calculateQueryTask(it)
                QueryTask.QueryType.DLQuery -> return;

            }
        }
    }
}