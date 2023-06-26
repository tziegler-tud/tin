package tin.services.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tin.services.internal.DijkstraQueryAnsweringService

@Service
class SchedulerService(
    // Repositories
) {
    @Autowired
    private lateinit var dijkstraQueryAnsweringService: DijkstraQueryAnsweringService

    // every 5sec, check for a scheduled queryTask to process
    @Scheduled(cron = "0/5 * * * * *")
    fun checkForQueryTask() {

    }
}