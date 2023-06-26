package tin.services.tintheweb

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import tin.model.technical.QueryTask
import tin.model.technical.QueryTaskRepository

@Service
class QueryTaskService(
    // Repositories
    private val queryTaskRepository: QueryTaskRepository,
) {

    fun getAllQueryTasks(): List<QueryTask> {
        return queryTaskRepository.findAll()
    }

    fun createQueryTask(): QueryTask? {
        return TODO()
    }
}

