package tin.controller.tintheweb.api.v1

import org.springframework.web.bind.annotation.*
import tin.data.tintheweb.settings.ApiSettingsData
import tin.data.tintheweb.settings.SettingsInfoData
import tin.services.Task.TaskService

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