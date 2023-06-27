package tin.services.internal.queryTask

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tin.model.technical.QueryResult
import tin.model.technical.QueryTask
import tin.model.technical.QueryTaskRepository
import tin.services.internal.DijkstraQueryAnsweringService

@Service
class QueryTaskService(
    private val queryTaskRepository: QueryTaskRepository
) {
    @Autowired
    lateinit var dijkstraQueryAnsweringService: DijkstraQueryAnsweringService

    private fun findOldestQueryTask(): QueryTask? {
        return queryTaskRepository.findFirstByOrderByCreatedAtAsc()
    }

    fun processQueryTask(queryTask: QueryTask): QueryResult {
        dijkstraQueryAnsweringService.calculateQueryTask(queryTask)
        return TODO()
    }


}