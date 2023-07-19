package tin.controller.tintheweb

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import tin.data.tintheweb.queryTask.QueryTaskCompleteData
import tin.data.tintheweb.queryTask.QueryTaskCreateData
import tin.services.tintheweb.QueryTaskService

@RestController
class QueryTaskController(
    private val queryTaskService: QueryTaskService
) {

    @GetMapping("query-task/get-all-tasks")
    fun getAllQueryTasks(): List<QueryTaskCompleteData> {
        return queryTaskService.getAllQueryTasks().map(::QueryTaskCompleteData)
    }

    @GetMapping("query-task/get-by-filter")
    fun getQueryTasksByFilter(): List<QueryTaskCompleteData> {
        return TODO()
    }

    @PostMapping("query-task/generate")
    @Transactional
    fun createQueryTask(@RequestBody data: QueryTaskCreateData): QueryTaskCompleteData {
        return QueryTaskCompleteData(queryTaskService.createQueryTask(data))
    }
}