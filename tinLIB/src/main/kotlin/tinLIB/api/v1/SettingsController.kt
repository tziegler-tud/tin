package tinLIB.api.v1

import org.springframework.web.bind.annotation.*
import tinLIB.data.api.settings.ApiSettingsData
import tinLIB.data.api.settings.SettingsInfoData
import tinLIB.services.Task.TaskService

@RestController
@RequestMapping("/api/v1/settings")
class SettingsController(
    private val taskService: TaskService
) {

    @GetMapping("/all")
    fun getTasks(): SettingsInfoData {

        return SettingsInfoData();
    }

    @PostMapping("/set")
    fun addTask(@RequestBody data: ApiSettingsData): SettingsInfoData {
        return SettingsInfoData();
    }
}