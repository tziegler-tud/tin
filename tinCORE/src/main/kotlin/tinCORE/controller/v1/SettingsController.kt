package tinCORE.controller.v1

import org.springframework.web.bind.annotation.*
import tinCORE.data.api.settings.ApiSettingsData
import tinCORE.data.api.settings.SettingsInfoData
import tinCORE.services.Task.TaskService

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