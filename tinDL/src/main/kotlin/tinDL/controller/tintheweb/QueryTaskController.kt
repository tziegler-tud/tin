package tinDL.controller.tintheweb

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import tinDL.data.tintheweb.DLqueryTask.DLQueryTaskCreateData
import tinDL.data.tintheweb.DLqueryTask.DLRegularPathQueryTaskCompleteData
import tinDL.data.tintheweb.queryTask.RegularPathQueryTaskCompleteData
import tinDL.data.tintheweb.queryTask.QueryTaskCreateData
import tinDL.services.tintheweb.QueryTaskService

@RestController
class QueryTaskController(
    private val queryTaskService: QueryTaskService
) {

    @GetMapping("query-task/get-all-tasks")
    fun getAllQueryTasks(): List<RegularPathQueryTaskCompleteData> {
        return queryTaskService.getAllQueryTasks().map(::RegularPathQueryTaskCompleteData)
    }

    @GetMapping("query-task/get-by-filter")
    fun getQueryTasksByFilter(): List<RegularPathQueryTaskCompleteData> {
        return TODO()
    }

    @PostMapping("query-task/generate")
    @Transactional
    fun createQueryTask(@RequestBody data: QueryTaskCreateData): RegularPathQueryTaskCompleteData {
        return RegularPathQueryTaskCompleteData(queryTaskService.createQueryTask(data))
    }

    @PostMapping("query-task/dl/generate")
    @Transactional
    fun createQueryTask(@RequestBody data: DLQueryTaskCreateData): DLRegularPathQueryTaskCompleteData {
        return DLRegularPathQueryTaskCompleteData(queryTaskService.createDLQueryTask(data))
    }
}